package com.softawii.capivara.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class SocialMetrics {

    // General Count
    private final AtomicLong parseCount;

    public SocialMetrics(MeterRegistry registry) {
        // Injected
        this.parseCount = registry.gauge("social.twitter.parse", new AtomicLong(0L));
    }

    public void newTwitterParse() {
        this.parseCount.addAndGet(1L);
    }
}
