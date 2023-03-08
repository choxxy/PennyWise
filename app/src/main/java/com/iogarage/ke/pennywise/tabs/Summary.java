package com.iogarage.ke.pennywise.tabs;

/**
 * Created by choxxy on 19/11/2016.
 */

public class Summary {

    private float amount;
    private String desc;
    private int color;

    public Summary(float amount, String desc, int color) {
        this.amount = amount;
        this.desc = desc;
        this.color = color;
    }

    public float getAmount() {
        return amount;
    }

    public String getDesc() {
        return desc;
    }

    public int getColor() {
        return color;
    }
}
