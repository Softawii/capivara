package com.softawii.capivara.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class ServerMetrics {

    private final AtomicLong guilds;

    public ServerMetrics(MeterRegistry registry) {
        this.guilds = registry.gauge("discord.guild.count", new AtomicLong(0L));
    }

    public void guildCount(long count) {
        this.guilds.set(count);
    }
}
