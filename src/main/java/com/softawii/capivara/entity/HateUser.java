package com.softawii.capivara.entity;

import net.dv8tion.jda.api.entities.User;

import java.math.BigInteger;

public class HateUser {
    private User user;
    private BigInteger messageCount;
    private BigInteger hateCount;
    private Double hate;

    public HateUser(User user, BigInteger messageCount, BigInteger hateCount, Double hate) {
        this.user = user;
        this.messageCount = messageCount;
        this.hateCount = hateCount;
        this.hate = hate;
    }

    public User getUser() {
        return user;
    }

    public BigInteger getMessageCount() {
        return messageCount;
    }

    public BigInteger getHateCount() {
        return hateCount;
    }

    public Double getHate() {
        return hate;
    }
}
