package com.softawii.capivara.entity;

import java.math.BigInteger;

public class HateStats {
    private final BigInteger messageCount;
    private final BigInteger hateCount;
    private final Double hate;

    public HateStats(BigInteger messageCount, BigInteger hateCount, Double hate) {
        this.messageCount = messageCount;
        this.hateCount = hateCount;
        this.hate = hate;
    }
    @Override
    public String toString() {
        return "HateStats{" +
                ", messageCount=" + messageCount +
                ", hateCount=" + hateCount +
                ", hate=" + hate +
                '}';
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