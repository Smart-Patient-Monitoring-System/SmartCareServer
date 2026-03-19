package com.example.mainservice.service;

import com.example.mainservice.entity.Appointment;
import com.example.mainservice.entity.Payment;
import com.example.mainservice.entity.enums.PaymentStatus;
import com.example.mainservice.repository.AppointmentRepository;
import com.example.mainservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${payhere.merchantId}")
    private String merchantId;

    @Value("${payhere.merchantSecret}")
    private String merchantSecret;

    @Value("${payhere.returnUrl}")
    private String returnUrl;

    @Value("${payhere.cancelUrl}")
    private String cancelUrl;

    @Value("${payhere.notifyUrl}")
    private String notifyUrl;

    @Value("${payhere.checkoutUrl:https://sandbox.payhere.lk/pay/checkout}")
    private String checkoutUrl;

    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;
    private final DoctorAvailabilityService doctorAvailabilityService;

    /* ======================================================
       IPN SUPPORT METHODS (UNCHANGED)
       ====================================================== */

    public void updatePaymentStatusFromOrderId(String orderId, PaymentStatus status) {
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            payment.setPaymentStatus(status);
            paymentRepository.save(payment);
        });
    }

    public Long getAppointmentIdFromOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(p -> p.getAppointment().getId())
                .orElse(null);
    }

    /* ======================================================
       BUILD PAYMENT FORM (FIXED ORDER_ID LOGIC)
       ====================================================== */

    public String buildPaymentForm(Long appointmentId, String amountParam) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        double amount = (amountParam != null && !amountParam.isEmpty())
                ? Double.parseDouble(amountParam)
                : appointment.getDoctor().getConsultationFee();

        if (amount <= 0) amount = 500.00;

        /* -----------------------------------------
           1️⃣ CREATE PAYMENT - SET TO SUCCESS DIRECTLY
           ----------------------------------------- */
        Payment payment = Payment.builder()
                .appointment(appointment)
                .amount(amount)
                .paymentGateway("PAYHERE_BYPASS")
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();
        paymentRepository.save(payment);

        /* -----------------------------------------
           2️⃣ GENERATE ORDER ID (USING APPOINTMENT ID)
           ----------------------------------------- */
        String orderId = "ORDER_" + appointment.getId() + "_" + System.currentTimeMillis();

        /* -----------------------------------------
           3️⃣ SAVE ORDER ID INTO BOTH TABLES & MARK SUCCESS
           ----------------------------------------- */
        payment.setOrderId(orderId);
        paymentRepository.save(payment);

        appointment.setOrderId(orderId);
        appointment.setPaymentStatus(PaymentStatus.SUCCESS);
        appointmentRepository.save(appointment);

        /* -----------------------------------------
           4️⃣ MARK SLOT AS BOOKED
           ----------------------------------------- */
        if (appointment.getAvailability() != null) {
            doctorAvailabilityService.markSlotBooked(appointment.getAvailability().getId());
        }

        /* -----------------------------------------
           5️⃣ REDIRECT TO FRONTEND SUCCESS PAGE
           ----------------------------------------- */
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Redirecting to Success</title>
                </head>
                <body>
                    <h2>Payment confirmed. Redirecting...</h2>
                    <script>
                        window.location.href = "http://localhost:5173/#/payment-success";
                    </script>
                </body>
                </html>
                """;
    }

    /* ======================================================
       HASHING (UNCHANGED)
       ====================================================== */

    public String generateMd5Hash(String merchantId, String orderId, String amount, String currency) {
        String md5Input = merchantId + orderId + amount + currency + md5(merchantSecret);
        return md5(md5Input);
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, digest);
            String hash = no.toString(16);
            while (hash.length() < 32) hash = "0" + hash;
            return hash.toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
