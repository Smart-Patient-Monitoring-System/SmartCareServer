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

    private final AppointmentRepository appointmentRepository;
    private final PaymentRepository paymentRepository;

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
           1️⃣ CREATE PAYMENT FIRST
           ----------------------------------------- */
        Payment payment = Payment.builder()
                .appointment(appointment)
                .amount(amount)
                .paymentGateway("PAYHERE")
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        paymentRepository.save(payment);

        /* -----------------------------------------
           2️⃣ GENERATE ORDER ID (USING APPOINTMENT ID)
           ----------------------------------------- */
        String orderId = "ORDER_" + appointment.getId() + "_" + System.currentTimeMillis();

        /* -----------------------------------------
           3️⃣ SAVE ORDER ID INTO BOTH TABLES
           ----------------------------------------- */
        payment.setOrderId(orderId);
        paymentRepository.save(payment);

        appointment.setOrderId(orderId);
        appointment.setPaymentStatus(PaymentStatus.PENDING);
        appointmentRepository.save(appointment);

        /* -----------------------------------------
           4️⃣ PAYHERE FORM DATA
           ----------------------------------------- */
        String currency = "LKR";
        String amountFormatted = String.format("%.2f", amount);
        String hash = generateMd5Hash(merchantId, orderId, amountFormatted, currency);

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Redirecting to PayHere</title>
                </head>
                <body onload="document.forms[0].submit()">
                    <form method="post" action="https://sandbox.payhere.lk/pay/checkout">
                        <input type="hidden" name="merchant_id" value="%s"/>
                        <input type="hidden" name="return_url" value="%s"/>
                        <input type="hidden" name="cancel_url" value="%s"/>
                        <input type="hidden" name="notify_url" value="%s"/>
                        <input type="hidden" name="order_id" value="%s"/>
                        <input type="hidden" name="items" value="Doctor Appointment"/>
                        <input type="hidden" name="currency" value="%s"/>
                        <input type="hidden" name="amount" value="%s"/>
                        <input type="hidden" name="hash" value="%s"/>
                        <input type="hidden" name="first_name" value="Patient"/>
                        <input type="hidden" name="last_name" value="User"/>
                        <input type="hidden" name="email" value="test@test.com"/>
                        <input type="hidden" name="phone" value="0770000000"/>
                        <input type="hidden" name="address" value="Colombo"/>
                        <input type="hidden" name="city" value="Colombo"/>
                        <input type="hidden" name="country" value="Sri Lanka"/>
                    </form>
                </body>
                </html>
                """.formatted(
                merchantId,
                returnUrl,
                cancelUrl,
                notifyUrl,
                orderId,
                currency,
                amountFormatted,
                hash
        );
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
