package com.example.mainservice.service;

import com.example.mainservice.entity.Appointment;
import com.example.mainservice.entity.Payment;
import com.example.mainservice.entity.enums.PaymentStatus;
import com.example.mainservice.repository.AppointmentRepository;
import com.example.mainservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;

    @Value("${payhere.merchantId:1234667}")
    private String merchantId;

    @Value("${payhere.merchantSecret:MTM3MzkzOTM2MzY0NzMwMzU3OTEyOTM0Njg5MjIxMzMxMjY5NjEz}")
    private String merchantSecret;

    @Value("${payhere.returnUrl:http://localhost:5173/#/payment-success}") // where user goes after success
    private String returnUrl;

    @Value("${payhere.cancelUrl:http://localhost:5173/#/payment-cancel}")
    private String cancelUrl;

    @Value("${payhere.notifyUrl:http://localhost:8080/api/payments/notify}") // your backend webhook
    private String notifyUrl;

    @Transactional
    public String buildPaymentForm(Long appointmentId, String requestedAmount) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        double amount = 0.0;
        try {
            amount = Double.parseDouble(requestedAmount);
        } catch (Exception e) {
            amount = 1500.00; // fallback default
        }

        // 1. Generate Order ID
        String orderId = "ORDER_" + appointment.getId() + "_" + System.currentTimeMillis();

        // 2. Create Payment record (PENDING)
        Payment payment = Payment.builder()
                .appointment(appointment)
                .orderId(orderId)
                .amount(amount)
                .paymentGateway("PAYHERE")
                .paymentStatus(PaymentStatus.PENDING)
                .paymentDate(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        // Link back to appointment
        appointment.setOrderId(orderId);
        appointmentRepository.save(appointment);

        // Format amount to 2 decimal places using US Locale to prevent comma (,) decimals in regions that use them
        String formattedAmount = String.format(java.util.Locale.US, "%.2f", amount);

        // 3. Generate MD5 Hash
        String hash = generateHash(merchantId, orderId, formattedAmount, "LKR", merchantSecret);

        // 4. Return HTML Form that auto-submits to PayHere Sandbox
        return "<html><body onload='document.forms[0].submit()'>" +
                "<form method='POST' action='https://sandbox.payhere.lk/pay/checkout'>" +
                "<input type='hidden' name='merchant_id' value='" + merchantId + "'>" +
                "<input type='hidden' name='return_url' value='" + returnUrl + "'>" +
                "<input type='hidden' name='cancel_url' value='" + cancelUrl + "'>" +
                "<input type='hidden' name='notify_url' value='" + notifyUrl + "'>" +

                "<input type='hidden' name='order_id' value='" + orderId + "'>" +
                "<input type='hidden' name='items' value='Doctor Appointment #" + appointmentId + "'>" +
                "<input type='hidden' name='currency' value='LKR'>" +
                "<input type='hidden' name='amount' value='" + formattedAmount + "'>" +

                "<input type='hidden' name='first_name' value='Patient'>" +
                "<input type='hidden' name='last_name' value=''>" +
                "<input type='hidden' name='email' value='patient@example.com'>" +
                "<input type='hidden' name='phone' value='0771234567'>" +
                "<input type='hidden' name='address' value='Colombo'>" +
                "<input type='hidden' name='city' value='Colombo'>" +
                "<input type='hidden' name='country' value='Sri Lanka'>" +

                "<input type='hidden' name='hash' value='" + hash + "'>" +
                "</form>" +
                "<h2>Redirecting to Secure Payment Gateway...</h2>" +
                "</body></html>";
    }

    private String generateHash(String merchantId, String orderId, String formattedAmount, String currency, String merchantSecret) {
        try {
            String hashedSecret = getMd5(merchantSecret).toUpperCase();
            String hashString = merchantId + orderId + formattedAmount + currency + hashedSecret;
            return getMd5(hashString).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }

    private String getMd5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(input.getBytes());
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
