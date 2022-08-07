package com.softawii.capivara.entity;

import com.softawii.capivara.core.EmbedManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.Modal;

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

    // Region Show

    public MessageEmbed show(Guild guild) {
        EmbedBuilder Builder = new EmbedBuilder();

        Builder.setTitle(String.format("Configurations to \"%s\"", guild.getCategoryById(categoryId).getName()));

        Builder.addField("Main Channel: ", guild.getVoiceChannelById(voiceId).getAsMention(), false);
        Builder.addField("Default: ", idle, true);
        Builder.addField("Default Playing: ", playing, true);
        Builder.addField("Default Streaming: ", streaming, true);

        return Builder.build();
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
