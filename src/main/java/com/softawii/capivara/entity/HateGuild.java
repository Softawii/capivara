package com.softawii.capivara.entity;

import net.dv8tion.jda.api.entities.Guild;

public class HateGuild {

    private Guild guild;
    private HateStats stats;

    public HateGuild(Guild guild, HateStats stats) {
        this.guild = guild;
        this.stats = stats;
    }

    public Guild getGuild() {
        return guild;
    }

    public HateStats getStats() {
        return stats;
    }
}
