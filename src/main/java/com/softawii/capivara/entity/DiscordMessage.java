package com.softawii.capivara.entity;

import net.dv8tion.jda.api.entities.Message;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.OffsetDateTime;

@Entity
public class DiscordMessage {

    @Id
    private Long messageId;
    @Column
    private Long userId;

    @Column
    private Long guildId;

    @Column
    private Long channelId;

    @Column
    private String content;

    @Column
    private OffsetDateTime timeCreated;

    @Column
    private OffsetDateTime timeEdited;

    @Column(nullable = false)
    private Boolean checked;

    @Column(nullable = true)
    private Boolean hate;

    @Column(nullable = true)
    private Boolean hateOpenai;

    public DiscordMessage() {
    }

    public DiscordMessage(Message message, boolean checked, boolean hate, boolean hateOpenai) {
        this.messageId = message.getIdLong();
        this.userId = message.getAuthor().getIdLong();
        this.guildId = message.getGuild().getIdLong();
        this.channelId = message.getChannel().getIdLong();
        this.content = message.getContentRaw();
        this.timeCreated = message.getTimeCreated();
        this.timeEdited = message.isEdited() ? message.getTimeEdited() : message.getTimeCreated();
        this.checked = false;
        this.hate = hate;
        this.hateOpenai = hateOpenai;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getGuildId() {
        return guildId;
    }

    public void setGuildId(Long guildId) {
        this.guildId = guildId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public OffsetDateTime getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(OffsetDateTime timeCreated) {
        this.timeCreated = timeCreated;
    }

    public OffsetDateTime getTimeEdited() {
        return timeEdited;
    }

    public void setTimeEdited(OffsetDateTime timeEdited) {
        this.timeEdited = timeEdited;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isHate() {
        return hate;
    }

    public void setHate(boolean hate) {
        this.hate = hate;
    }

    public boolean isHateOpenai() {
        return hateOpenai;
    }

    public void setHateOpenai(boolean hateOpenai) {
        this.hateOpenai = hateOpenai;
    }

    @Override
    public String toString() {
        return "DiscordMessage{" +
                "messageId=" + messageId +
                ", userId=" + userId +
                ", guildId=" + guildId +
                ", channelId=" + channelId +
                ", content='" + content + '\'' +
                ", timeCreated=" + timeCreated +
                ", timeEdited=" + timeEdited +
                ", checked=" + checked +
                ", hate=" + hate +
                ", hateOpenai=" + hateOpenai +
                '}';
    }
}
