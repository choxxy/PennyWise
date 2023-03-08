package com.iogarage.ke.pennywise;

import com.iogarage.ke.pennywise.entities.Payment;

/**
 * Created by IOGarage on 10/23/2016.
 */
public class PaymentSelected {
    private Payment payment;

    public PaymentSelected(Payment payment) {
        this.payment = payment;
    }

    public Payment getPayment() {
        return payment;
    }
}
