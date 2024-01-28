package com.softawii.capivara.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TwitterTransform {
    @Id
    private Long guildId;

    public TwitterTransform() {
    }

    public TwitterTransform(Long guildId) {
        this.guildId = guildId;
    }

    public Long getGuildId() {
        return guildId;
    }

    public void setGuildId(Long guildId) {
        this.guildId = guildId;
    }
}
