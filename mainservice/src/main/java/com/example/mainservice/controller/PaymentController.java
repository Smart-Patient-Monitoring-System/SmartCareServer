package com.example.mainservice.controller;

import com.example.mainservice.entity.Appointment;
import com.example.mainservice.entity.Payment;
import com.example.mainservice.entity.enums.AppointmentStatus;
import com.example.mainservice.entity.enums.PaymentStatus;
import com.example.mainservice.repository.AppointmentRepository;
import com.example.mainservice.repository.PaymentRepository;
import com.example.mainservice.service.DoctorAvailabilityService;
import com.example.mainservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * Generates and returns the PayHere HTML form that auto-submits.
     * Called by the patient frontend after booking.
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
     * Patient can poll this to check their payment status.
     */
    @GetMapping("/status/{appointmentId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
        if (appointment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
                "appointmentId", appointmentId,
                "paymentStatus", appointment.getPaymentStatus().name(),
                "appointmentStatus", appointment.getAppointmentStatus().name()
        ));
    }

    /**
     * POST /api/payments/notify
     * PayHere IPN (Instant Payment Notification) webhook.
     * PayHere POSTs to this endpoint after each payment attempt.
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
        System.out.println("=== PayHere IPN ===");
        System.out.println("order_id=" + orderId + " status=" + statusCode + " payment_id=" + paymentId);

        try {
            // Extract appointment ID from order_id format: "ORDER_{appointmentId}_{timestamp}"
            String[] parts = orderId.split("_");
            if (parts.length < 2) {
                throw new RuntimeException("Invalid order_id format: " + orderId);
            }
            Long appointmentId = Long.parseLong(parts[1]);

            Appointment appointment = appointmentRepository.findById(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));

            // Create payment record
            Payment payment = new Payment();
            payment.setAppointment(appointment);
            payment.setAmount(Double.parseDouble(amount));
            payment.setOrderId(orderId);
            payment.setTransactionId(paymentId);
            payment.setPaymentGateway("PayHere");

            if ("2".equals(statusCode)) {
                // SUCCESS — mark slot booked, update statuses
                if (appointment.getAvailability() != null) {
                    try {
                        doctorAvailabilityService.markSlotBooked(appointment.getAvailability().getId());
                    } catch (Exception e) {
                        System.out.println("Slot already booked or not found: " + e.getMessage());
                    }
                }
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                appointment.setPaymentStatus(PaymentStatus.SUCCESS);
                System.out.println("Payment SUCCESS for appointment " + appointmentId);

            } else if ("0".equals(statusCode)) {
                // PENDING — payment being processed
                payment.setPaymentStatus(PaymentStatus.PENDING);
                System.out.println("Payment PENDING for appointment " + appointmentId);

            } else {
                // FAILED / CANCELLED — release the slot
                if (appointment.getAvailability() != null) {
                    try {
                        doctorAvailabilityService.markSlotAvailable(appointment.getAvailability().getId());
                    } catch (Exception e) {
                        System.out.println("Could not release slot: " + e.getMessage());
                    }
                }
                payment.setPaymentStatus(PaymentStatus.FAILED);
                appointment.setPaymentStatus(PaymentStatus.FAILED);
                System.out.println("Payment FAILED for appointment " + appointmentId + " (status=" + statusCode + ")");
            }

            paymentRepository.save(payment);
            appointmentRepository.save(appointment);

        } catch (Exception e) {
            System.out.println("Error handling PayHere IPN: " + e.getMessage());
            e.printStackTrace();
        }

        return "ok"; // PayHere requires "ok" response
    }
}
