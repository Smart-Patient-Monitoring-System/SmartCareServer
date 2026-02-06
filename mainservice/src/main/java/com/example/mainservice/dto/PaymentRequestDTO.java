package com.example.mainservice.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {
    private double amount;
    private String paymentGateway;
    private boolean success; // true if payment succeeded
}