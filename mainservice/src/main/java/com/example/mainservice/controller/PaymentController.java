package com.example.mainservice.controller;

import com.example.mainservice.entity.Appointment;
import com.example.mainservice.entity.Payment;
import com.example.mainservice.entity.enums.PaymentStatus;
import com.example.mainservice.repository.AppointmentRepository;
import com.example.mainservice.repository.PaymentRepository;
import com.example.mainservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentController {

    private final PaymentService paymentService;
    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;

    // Build payment form for frontend
    @GetMapping("/pay/{appointmentId}")
    public String pay(@PathVariable Long appointmentId,
                      @RequestParam(required = false) String amount) {
        System.out.println("=== /pay called for appointmentId: " + appointmentId + " with amount: " + amount);
        return paymentService.buildPaymentForm(appointmentId, amount);
    }

    // PayHere IPN notification
    @PostMapping("/notify")
    public String notifyPayment(
            @RequestParam("merchant_id") String merchantId,
            @RequestParam("order_id") String orderId,
            @RequestParam("payhere_amount") String amount,
            @RequestParam("status_code") String statusCode,
            @RequestParam("md5sig") String md5sig,
            @RequestParam("payment_id") String paymentId,
            @RequestParam("method") String method) {

        System.out.println("=== PayHere IPN Notification Received ===");
        System.out.println("merchant_id: " + merchantId);
        System.out.println("order_id: " + orderId);
        System.out.println("amount: " + amount);
        System.out.println("status_code: " + statusCode);
        System.out.println("md5sig: " + md5sig);
        System.out.println("payment_id: " + paymentId);
        System.out.println("method: " + method);

        try {
            // Extract numeric appointment ID from order_id
            // Example: "ORDER_5_1769372867357" -> "5"
            String[] parts = orderId.split("_");
            if (parts.length < 2) {
                throw new RuntimeException("Invalid order_id format: " + orderId);
            }
            Long appointmentId = Long.parseLong(parts[1]);

            // Find appointment by extracted ID
            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + appointmentId));

            // Create Payment entity
            Payment payment = new Payment();
            payment.setAppointment(appointment);
            payment.setAmount(Double.parseDouble(amount));
            payment.setOrderId(orderId);
            payment.setTransactionId(paymentId);
            payment.setPaymentGateway("PayHere");

            // Set payment and appointment status
            if ("2".equals(statusCode)) { // 2 = success
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                appointment.setPaymentStatus(PaymentStatus.SUCCESS);
                System.out.println("Payment SUCCESS for appointment " + appointmentId);
            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                appointment.setPaymentStatus(PaymentStatus.FAILED);
                System.out.println("Payment FAILED for appointment " + appointmentId);
            }

            // Save payment and update appointment
            paymentRepository.save(payment);
            appointmentRepository.save(appointment);

            System.out.println("Payment saved: " + payment);
            System.out.println("Appointment updated: " + appointment);

        } catch (Exception e) {
            System.out.println("Error handling PayHere IPN: " + e.getMessage());
            e.printStackTrace();
        }

        // Return 'ok' to PayHere
        return "ok";
    }
}

