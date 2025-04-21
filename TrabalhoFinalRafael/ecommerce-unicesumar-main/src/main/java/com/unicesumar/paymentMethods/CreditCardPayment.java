package com.unicesumar.paymentMethods;

import java.util.UUID;

public class CreditCardPayment implements PaymentMethod {
    @Override
    public String payment(double amount) {
        String transactionCode = "CC-" + UUID.randomUUID().toString().substring(0, 12);

        System.out.println("Pagamento concluido pelo Cartão de Crédito.");
        System.out.println("Token de autorizacao: " + transactionCode);
        return transactionCode;
    }
}