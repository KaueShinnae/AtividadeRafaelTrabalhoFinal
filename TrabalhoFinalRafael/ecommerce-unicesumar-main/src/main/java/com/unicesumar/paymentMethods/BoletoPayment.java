package com.unicesumar.paymentMethods;

import java.util.UUID;

public class BoletoPayment implements PaymentMethod {
    @Override
    public String payment(double amount) {
        String transactionCode = "BOL" + UUID.randomUUID().toString().substring(0, 8) +
                               UUID.randomUUID().toString().substring(0, 4);
        
        System.out.println("Pagamento confirmado pelo Boleto.");
        System.out.println("Código de Barras: " + transactionCode);
        return transactionCode;
    }
}