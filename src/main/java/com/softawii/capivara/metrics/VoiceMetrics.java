package com.softawii.capivara.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class VoiceMetrics {

    private final MeterRegistry registry;

    // Agent
    private final AtomicLong agentCount;
    private final AtomicLong agentAdd;
    private final AtomicLong agentRemove;
    private final AtomicLong agentUpdate;

    // Master
    private final AtomicLong masterCount;
    private final AtomicLong masterAdd;
    private final AtomicLong masterRemove;
    private final AtomicLong masterUpdate;

    public VoiceMetrics(MeterRegistry registry) {
        this.registry = registry;

        this.agentCount = this.registry.gauge("voice.agents.active", new AtomicLong(0L));
        this.agentAdd = this.registry.gauge("voice.agents.add", new AtomicLong(0L));
        this.agentRemove = this.registry.gauge("voice.agents.remove", new AtomicLong(0L));
        this.agentUpdate = this.registry.gauge("voice.agents.update", new AtomicLong(0L));

        this.masterCount = this.registry.gauge("voice.master.active", new AtomicLong(0L));
        this.masterAdd = this.registry.gauge("voice.master.add", new AtomicLong(0L));
        this.masterRemove = this.registry.gauge("voice.master.remove", new AtomicLong(0L));
        this.masterUpdate = this.registry.gauge("voice.master.update", new AtomicLong(0L));
    }

    public void agentCount(Long count) {
        this.agentCount.set(count);
    }

    public void agentCreated() {
        this.agentAdd.addAndGet(1L);
    }

    public void agentDestroyed() {
        this.agentRemove.addAndGet(1L);
    }

    public void agentUpdate() {
        this.agentUpdate.addAndGet(1L);
    }

    public void masterCount(Long count) {
        this.masterCount.set(count);
    }

    public void masterCreated() {
        this.masterAdd.addAndGet(1L);
    }

    public void masterDestroyed() {
        this.masterRemove.addAndGet(1L);
    }

    public void masterUpdate() {
        this.masterUpdate.addAndGet(1L);
    }
}
