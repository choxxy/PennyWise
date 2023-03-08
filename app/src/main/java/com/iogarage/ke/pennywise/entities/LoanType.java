package com.iogarage.ke.pennywise.entities;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by Root on 9/15/2016.
 */


@Entity( nameInDb = "loantypes")
public class LoanType {
    @Id
    Long id;
    int type;
    String description;
    int imageresource;

    @Generated(hash = 409445767)
    public LoanType(Long id, int type, String description, int imageresource) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.imageresource = imageresource;
    }

    @Generated(hash = 410387685)
    public LoanType() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImageresource() {
        return this.imageresource;
    }

    public void setImageresource(int imageresource) {
        this.imageresource = imageresource;
    }


}
