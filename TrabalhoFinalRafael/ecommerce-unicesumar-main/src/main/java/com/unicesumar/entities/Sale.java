package com.unicesumar.entities;

import com.unicesumar.paymentMethods.PaymentType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Sale extends Entity {
    private final User user;
    private final List<Product> products;
    private final PaymentType paymentMethod;
    private final Date saleDate;
    private double totalAmount;

    public Sale(UUID uuid, User user, List<Product> products, PaymentType paymentMethod, Date saleDate) {
        super(uuid);
        this.user = user;
        this.products = products;
        this.paymentMethod = paymentMethod;
        this.saleDate = saleDate;
        this.calculateTotal();
    }

    public Sale(User user, List<Product> products, PaymentType paymentMethod) {
        this.user = user;
        this.products = products;
        this.paymentMethod = paymentMethod;
        this.saleDate = new Date();
        this.calculateTotal();
    }

    private void calculateTotal() {
        this.totalAmount = 0;
        for (Product product : products) {
            this.totalAmount += product.getPrice();
        }
    }

    public User getUser() {
        return user;
    }

    public List<Product> getProducts() {
        return new ArrayList<>(products);
    }

    public PaymentType getPaymentMethod() {
        return this.paymentMethod;
    }

    public Date getSaleDate() {
        return this.saleDate;
    }
    
    public double getTotalAmount() {
        return this.totalAmount;
    }
}
