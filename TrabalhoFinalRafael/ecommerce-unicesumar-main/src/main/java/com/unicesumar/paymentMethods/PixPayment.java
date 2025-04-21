package com.unicesumar.paymentMethods;

import java.util.UUID;

public class PixPayment implements PaymentMethod {
    @Override
    public String payment(double amount) {
        String transactionCode = UUID.randomUUID().toString().substring(0, 8) + "." +
                               UUID.randomUUID().toString().substring(0, 4) + "." +
                               UUID.randomUUID().toString().substring(0, 4) + "." +
                               UUID.randomUUID().toString().substring(0, 8);
        
        System.out.println("Pagamento efetuado pelo PIX. Token de Autenticação: " + transactionCode);
        return transactionCode;
    }
}