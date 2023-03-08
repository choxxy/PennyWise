package com.iogarage.ke.pennywise.entities;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;

@Entity( nameInDb = "payments")
public class Payment {

    @Id
    private Long id;
    private Long transactionId;
    private Date paymentdate;
    private String description;
    private String note;
    private Double amountpaid;
    @Generated(hash = 2059982885)
    public Payment(Long id, Long transactionId, Date paymentdate,
            String description, String note, Double amountpaid) {
        this.id = id;
        this.transactionId = transactionId;
        this.paymentdate = paymentdate;
        this.description = description;
        this.note = note;
        this.amountpaid = amountpaid;
    }
    @Generated(hash = 1565471489)
    public Payment() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getTransactionId() {
        return this.transactionId;
    }
    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }
    public Date getPaymentdate() {
        return this.paymentdate;
    }
    public void setPaymentdate(Date paymentdate) {
        this.paymentdate = paymentdate;
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getNote() {
        return this.note;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public Double getAmountpaid() {
        return this.amountpaid;
    }
    public void setAmountpaid(Double amountpaid) {
        this.amountpaid = amountpaid;
    }


}
