package com.unicesumar;

import com.unicesumar.paymentMethods.BoletoPayment;
import com.unicesumar.paymentMethods.CreditCardPayment;
import com.unicesumar.paymentMethods.PaymentMethod;
import com.unicesumar.paymentMethods.PaymentType;
import com.unicesumar.paymentMethods.PixPayment;

public class PaymentMethodFactory {
    public static PaymentMethod create(PaymentType type) {
        // optei pela switch case pela dica que o Professor Rafael deu para o lucas
        switch (type) {
            case PIX:
                return new PixPayment();
            case BOLETO:
                return new BoletoPayment();
            case CARTAO:
                return new CreditCardPayment();
            default:
                return new PixPayment();
        }
    }
}