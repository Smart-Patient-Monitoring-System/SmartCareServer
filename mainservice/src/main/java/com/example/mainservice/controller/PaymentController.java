package com.example.mainservice.controller;

import com.example.mainservice.entity.Appointment;
import com.example.mainservice.entity.Payment;
import com.example.mainservice.entity.enums.PaymentStatus;
import com.example.mainservice.repository.AppointmentRepository;
import com.example.mainservice.repository.PaymentRepository;
import com.example.mainservice.service.DoctorAvailabilityService;
import com.example.mainservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;
    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;
    private final DoctorAvailabilityService doctorAvailabilityService;

    /**
     * GET /api/payments/pay/{appointmentId}?amount=...
     * Returns auto-submitting PayHere HTML form.
     * Called by patient frontend immediately after booking.
     */
    @GetMapping("/pay/{appointmentId}")
    public String pay(
            @PathVariable Long appointmentId,
            @RequestParam(required = false) String amount
    ) {
        System.out.println("=== /pay called for appointmentId: " + appointmentId + " amount: " + amount);
        return paymentService.buildPaymentForm(appointmentId, amount);
    }

    /**
     * GET /api/payments/status/{appointmentId}
     * Patient polls this to check payment result after returning from PayHere.
     */
    @GetMapping("/status/{appointmentId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long appointmentId) {
        Appointment a = appointmentRepository.findById(appointmentId).orElse(null);
        if (a == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of(
                "appointmentId", appointmentId,
                "paymentStatus", a.getPaymentStatus().name(),
                "appointmentStatus", a.getAppointmentStatus().name()
        ));
    }

    /**
     * POST /api/payments/notify
     * PayHere IPN (Instant Payment Notification) webhook.
     * PayHere POSTs here after every payment attempt.
     */
    @PostMapping("/notify")
    public String notifyPayment(
            @RequestParam("merchant_id") String merchantId,
            @RequestParam("order_id") String orderId,
            @RequestParam("payhere_amount") String amount,
            @RequestParam("status_code") String statusCode,
            @RequestParam("md5sig") String md5sig,
            @RequestParam("payment_id") String paymentId,
            @RequestParam("method") String method
    ) {
        System.out.println("=== PayHere IPN === order=" + orderId + " status=" + statusCode);

        try {
            // order_id format: "ORDER_{appointmentId}_{timestamp}"
            String[] parts = orderId.split("_");
            if (parts.length < 2) throw new RuntimeException("Invalid order_id: " + orderId);
            Long appointmentId = Long.parseLong(parts[1]);

            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));

            Payment payment = new Payment();
            payment.setAppointment(appointment);
            payment.setAmount(Double.parseDouble(amount));
            payment.setOrderId(orderId);
            payment.setTransactionId(paymentId);
            payment.setPaymentGateway("PayHere");

            if ("2".equals(statusCode)) {
                // SUCCESS
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                appointment.setPaymentStatus(PaymentStatus.SUCCESS);
                System.out.println("Payment SUCCESS for appointment " + appointmentId);
            } else if ("0".equals(statusCode)) {
                // PENDING
                payment.setPaymentStatus(PaymentStatus.PENDING);
                System.out.println("Payment PENDING for appointment " + appointmentId);
            } else {
                // FAILED / CANCELLED - release the slot so it can be booked again
                if (appointment.getAvailability() != null) {
                    try {
                        doctorAvailabilityService.markSlotAvailable(appointment.getAvailability().getId());
                    } catch (Exception e) {
                        System.out.println("Could not release slot: " + e.getMessage());
                    }
                }
                payment.setPaymentStatus(PaymentStatus.FAILED);
                appointment.setPaymentStatus(PaymentStatus.FAILED);
                System.out.println("Payment FAILED for appointment " + appointmentId);
            }

            paymentRepository.save(payment);
            appointmentRepository.save(appointment);

        } catch (Exception e) {
            System.out.println("IPN error: " + e.getMessage());
            e.printStackTrace();
        }

        return "ok"; // PayHere requires "ok" response
    }

    /**
     * GET /api/payments/dev-success-all
     * DEVELOPMENT ONLY — bypass PayHere webhook by manually marking all
     * PENDING appointments as SUCCESS. Use when ngrok is not available.
     */
    @GetMapping("/dev-success-all")
    public String devSuccessAll() {
        try {
            List<Appointment> pending = appointmentRepository.findByPaymentStatus(PaymentStatus.PENDING);
            int count = 0;
            for (Appointment a : pending) {
                a.setPaymentStatus(PaymentStatus.SUCCESS);
                appointmentRepository.save(a);

                Payment payment = new Payment();
                payment.setAppointment(a);
                payment.setAmount(a.getDoctor() != null ? a.getDoctor().getConsultationFee() : 1500.0);
                payment.setOrderId("DEV_BYPASS_" + a.getId());
                payment.setTransactionId("DEV_TXN_" + System.currentTimeMillis() + "_" + count);
                payment.setPaymentGateway("PayHere (Dev Bypass)");
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                paymentRepository.save(payment);
                count++;
            }
            return "Marked " + count + " pending appointments as PAID (dev bypass).";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
