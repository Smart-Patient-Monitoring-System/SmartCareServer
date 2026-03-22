package com.example.mainservice.controller;

import com.example.mainservice.entity.Appointment;
import com.example.mainservice.entity.Payment;
import com.example.mainservice.entity.enums.PaymentStatus;
import com.example.mainservice.repository.AppointmentRepository;
import com.example.mainservice.repository.PaymentRepository;
import com.example.mainservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*") // Allows the frontend to freely call the success verification
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;

    // Build payment form for frontend
    @GetMapping("/pay/{appointmentId}")
    public String pay(@PathVariable Long appointmentId,
                      @RequestParam(required = false) String amount) {
        System.out.println("=== /pay called for appointmentId: " + appointmentId + " | amount: " + amount + " ===");
        return paymentService.buildPaymentForm(appointmentId, amount);
    }

    // Webhook called by PayHere servers in background
    @PostMapping("/notify")
    public void handlePayHereNotification(
            @RequestParam("merchant_id") String merchantId,
            @RequestParam("order_id") String orderId,
            @RequestParam("payhere_amount") String payhereAmount,
            @RequestParam("payhere_currency") String payhereCurrency,
            @RequestParam("status_code") String statusCode,
            @RequestParam("md5sig") String md5sig,
            @RequestParam(value = "custom_1", required = false) String custom1,
            @RequestParam(value = "custom_2", required = false) String custom2
    ) {
        System.out.println("=== PAYHERE NOTIFICATION RECEIVED ===");
        System.out.println("Order ID: " + orderId);
        System.out.println("Status Code: " + statusCode);

        // 1. Find the payment and appointment
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (payment == null) {
            System.err.println("Order not found: " + orderId);
            return;
        }

        Appointment appointment = payment.getAppointment();

        // 2. Validate hash (optional but recommended for production)
        // boolean isValid = paymentService.validateHash(...); 
        // if (!isValid) return;

        // 3. Update status based on statusCode
        // 2 = Success, 0 = Pending, -1 = Canceled, -2 = Failed, -3 = Chargedback
        if ("2".equals(statusCode)) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            appointment.setPaymentStatus(PaymentStatus.SUCCESS);
        } else if ("0".equals(statusCode)) {
            payment.setPaymentStatus(PaymentStatus.PENDING);
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            appointment.setPaymentStatus(PaymentStatus.FAILED);
        }

        paymentRepository.save(payment);
        appointmentRepository.save(appointment);
        System.out.println("=== PAYMENT SAVED -> " + payment.getPaymentStatus() + " ===");
    }

    // IMPORTANT: For development mostly!
    // Forces all pending payments to SUCCESS so you don't need real PayHere IPN configured.
    @GetMapping("/dev-success-all")
    public String forceSuccessAll() {
        System.out.println("=== DEV: FORCING ALL PENDING PAYMENTS TO SUCCESS ===");
        var payments = paymentRepository.findAll();
        int count = 0;
        for (Payment p : payments) {
            if (p.getPaymentStatus() == PaymentStatus.PENDING) {
                p.setPaymentStatus(PaymentStatus.SUCCESS);
                p.setPaymentDate(LocalDateTime.now());
                paymentRepository.save(p);

                Appointment a = p.getAppointment();
                if (a != null) {
                    a.setPaymentStatus(PaymentStatus.SUCCESS);
                    appointmentRepository.save(a);
                }
                count++;
            }
        }
        return "Updated " + count + " pending payments to SUCCESS.";
    }

    @GetMapping("/success/{orderId}")
    public ResponseEntity<Map<String, String>> verifyPaymentSuccess(@PathVariable String orderId) {
        System.out.println("=== FRONTEND VERIFY SUCCESS FOR ORDER: " + orderId + " ===");
        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);
        if (payment != null && payment.getPaymentStatus() == PaymentStatus.PENDING) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(payment);
            
            Appointment a = payment.getAppointment();
            if (a != null) {
                a.setPaymentStatus(PaymentStatus.SUCCESS);
                appointmentRepository.save(a);
            }
            return ResponseEntity.ok(Map.of("message", "Payment manually verified on frontend"));
        }
        return ResponseEntity.ok(Map.of("message", "Payment already processed or not found"));
    }
}
