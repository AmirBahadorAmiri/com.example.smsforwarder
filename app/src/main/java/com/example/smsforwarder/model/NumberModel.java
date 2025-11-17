package com.example.smsforwarder.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "number_tb")
public class NumberModel {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String forwardFrom,forwardTo;

    public NumberModel(String forwardFrom, String forwardTo) {
        this.forwardFrom = forwardFrom;
        this.forwardTo = forwardTo;
    }

    public int getId() {
        return id;
    }

    public String getForwardFrom() {
        return forwardFrom;
    }

    public void setForwardFrom(String forwardFrom) {
        this.forwardFrom = forwardFrom;
    }

    public String getForwardTo() {
        return forwardTo;
    }

    public void setForwardTo(String forwardTo) {
        this.forwardTo = forwardTo;
    }
}
