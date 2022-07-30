package com.softawii.capivara.entity;

import javax.persistence.*;

@Entity
public class VoiceHive {

    @Id
    Long categoryId;

    /**
     * This column is not necessary, but it's used to identify all hives in the guild.
     */
    @Column
    Long guildId;

    /**
     * Category needs to have a hive channel, this is the channel where the bot will listen
     */
    @Column
    private long voiceId;

    // region Constructors

    public VoiceHive() {
    }

    public VoiceHive(Long categoryId, Long guildId, long voiceId) {
        this.categoryId = categoryId;
        this.guildId = guildId;
        this.voiceId = voiceId;
    }

    // endregion

    // region Getters and Setters

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getGuildId() {
        return guildId;
    }

    public void setGuildId(Long guildId) {
        this.guildId = guildId;
    }

    public long getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(long voiceId) {
        this.voiceId = voiceId;
    }

    // endregion
}
