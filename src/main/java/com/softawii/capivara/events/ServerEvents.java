package com.softawii.capivara.events;

import com.softawii.capivara.metrics.ServerMetrics;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
public class ServerEvents extends ListenerAdapter {

    private final ServerMetrics metrics;
    private final JDA jda;

    public ServerEvents(JDA jda, ServerMetrics metrics) {
        this.metrics = metrics;
        this.jda = jda;

        jda.addEventListener(this);

        this.metrics.guildCount(jda.getGuilds().size());
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        this.metrics.guildCount(jda.getGuilds().size());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        this.metrics.guildCount(jda.getGuilds().size());
    }
}
