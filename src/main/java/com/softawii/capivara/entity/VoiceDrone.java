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
     *
     */
    @Column
    private Long chatId;

    /**
     * It is used to identify owner of the current worker
     * If it's "null", it means that the worker is not owned by anyone
     */
    @Column
    private Long ownerId;

    /**
     * It is used to identify the control panel of the current worker
     */
    @Column
    private Long controlPanel;

    /**
     * It is used to verify if the drone will be deleted when the voice channel is empty or not
     */
    @Column
    private Boolean permanent;

    /**
     * It is used when the owner leaves and there are still people on the voice channel
     * If it's "null", it means that the drone has an active owned
     */
    private Long claimMessage;

    // region Constructors

    public VoiceDrone() {
    }

    public VoiceDrone(Long channelId, Long chatId, Long ownerId, Long controlPanel) {
        this.channelId = channelId;
        this.ownerId = ownerId;
        this.chatId = chatId;
        this.controlPanel = controlPanel;
        this.permanent = false;
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

    public Boolean getPermanent() {
        return permanent;
    }

    public void setPermanent(Boolean permanent) {
        this.permanent = permanent;
    }

    public Boolean isPermanent() {
        return permanent;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Long getClaimMessage() {
        return claimMessage;
    }

    public void setClaimMessage(Long claimMessage) {
        this.claimMessage = claimMessage;
    }

    // endregion
}
