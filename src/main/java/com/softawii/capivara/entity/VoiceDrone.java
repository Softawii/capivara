package com.softawii.capivara.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class VoiceDrone {

    /**
     * It is used to identify the worker, it's a snowflake id
     */
    @Id
    private Long channelId;

    /**
     * It is used to identify owner of the current worker
     * If it's "null", it means that the worker is not owned by anyone
     */
    @Column
    private Long ownerId;

    @Column
    private Long controlPanel;

    // region Constructors

    public VoiceDrone() {
    }

    public VoiceDrone(Long channelId, Long ownerId, Long controlPanel) {
        this.channelId = channelId;
        this.ownerId = ownerId;
        this.controlPanel = controlPanel;
    }

    // endregion

    // region Getters and Setters

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getControlPanel() {
        return controlPanel;
    }

    public void setControlPanel(Long controlPanel) {
        this.controlPanel = controlPanel;
    }


    // endregion
}
