package com.softawii.capivara.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SocialParserConfig {
    @Id
    @Column(unique = true, nullable = false)
    private Long guildId;

    @Column()
    private boolean twitter = false; // default = false

    @Column()
    private boolean bsky = false; // default = false

    public SocialParserConfig() {
    }

    public SocialParserConfig(Long guildId) {
        this.guildId = guildId;
        this.twitter = false;
        this.bsky = false;
    }

    public Long getGuildId() {
        return guildId;
    }

    public void setGuildId(Long guildId) {
        this.guildId = guildId;
    }

    public boolean isTwitter() {
        return twitter;
    }

    public void setTwitter(boolean twitter) {
        this.twitter = twitter;
    }

    public boolean isBsky() {
        return bsky;
    }

    public void setBsky(boolean bsky) {
        this.bsky = bsky;
    }
}
