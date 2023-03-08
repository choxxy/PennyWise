package com.iogarage.ke.pennywise;

import com.iogarage.ke.pennywise.entities.Debt;

/**
 * Created by choxxy on 24/10/2016.
 */
public class DeleteTransaction {
    private Debt debt;

    public DeleteTransaction(Debt debt) {
        this.debt = debt;
    }

    public Debt getDebt() {
        return debt;
    }
}
