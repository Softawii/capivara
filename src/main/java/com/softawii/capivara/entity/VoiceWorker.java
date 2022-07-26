package com.softawii.capivara.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class VoiceWorker {

    /**
     * It is used to identify the worker, it's a snowflake id
     */
    @Id
    private long channelId;

    /**
     * It is used to identify owner of the current worker
     * If it's "null", it means that the worker is not owned by anyone
     */
    @Column
    private long OwnerId;

    // region Getters and Setters

    public long channelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public long OwnerId() {
        return OwnerId;
    }

    public void setOwnerId(long ownerId) {
        OwnerId = ownerId;
    }

    // endregion
}
