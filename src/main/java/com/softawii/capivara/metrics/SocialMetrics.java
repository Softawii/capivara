package com.softawii.capivara.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class SocialMetrics {

    // General Count
    private final AtomicLong parseTwitterCount;
    private final AtomicLong parseBskyCount;

    public SocialMetrics(MeterRegistry registry) {
        // Injected
        this.parseTwitterCount = registry.gauge("social.twitter.parse", new AtomicLong(0L));
        this.parseBskyCount = registry.gauge("social.bsky.parse", new AtomicLong(0L));
    }

    public void newTwitterParse() {
        this.parseTwitterCount.addAndGet(1L);
    }

    public void newBskyParse() {
        this.parseBskyCount.addAndGet(1L);
    }
}
