package com.softawii.capivara.entity;

import javax.persistence.*;

@Entity
public class VoiceHive {

    @Id
    private Long categoryId;

    /**
     * This column is not necessary, but it's used to identify all hives in the guild.
     */
    @Column
    private Long guildId;

    /**
     * Category needs to have a hive channel, this is the channel where the bot will listen
     */
    @Column
    private Long voiceId;

    /**
     * Temporary names
     */
    @Column
    private String idle;

    @Column
    private String playing;

    @Column
    private String streaming;

    // region Constructors

    public VoiceHive() {
    }

    public VoiceHive(Long categoryId, Long guildId, long voiceId, String idle, String playing, String streaming) {
        this.categoryId = categoryId;
        this.guildId = guildId;
        this.voiceId = voiceId;

        // Temporary names
        this.idle = idle;
        this.playing = playing;
        this.streaming = streaming;

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

    public void setVoiceId(Long voiceId) {
        this.voiceId = voiceId;
    }

    public String getIdle() {
        return idle;
    }

    public void setIdle(String idle) {
        this.idle = idle;
    }

    public String getPlaying() {
        return playing;
    }

    public void setPlaying(String playing) {
        this.playing = playing;
    }

    public String getStreaming() {
        return streaming;
    }

    public void setStreaming(String streaming) {
        this.streaming = streaming;
    }

    // endregion
}
