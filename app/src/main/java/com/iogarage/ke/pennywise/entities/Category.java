package com.iogarage.ke.pennywise.entities;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * Created by choxxy on 04/07/2017.
 */

@Entity(nameInDb = "Categories")
public class Category {

    @Id
    private Long id;
    private String title;
    @Generated(hash = 206903207)
    public Category(Long id, String title) {
        this.id = id;
        this.title = title;
    }
    @Generated(hash = 1150634039)
    public Category() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

}
