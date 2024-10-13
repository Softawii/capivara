package com.softawii.capivara.services;

import com.softawii.capivara.entity.VoiceDrone;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.metrics.VoiceMetrics;
import com.softawii.capivara.repository.VoiceDroneRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.management.openmbean.KeyAlreadyExistsException;

@Service
public class VoiceDroneService {

    private final VoiceMetrics metrics;
    private final VoiceDroneRepository voiceDroneRepository;

    public VoiceDroneService(VoiceDroneRepository voiceDroneRepository, VoiceMetrics metrics) {
        this.voiceDroneRepository = voiceDroneRepository;
        this.metrics = metrics;

        this.metrics.agentCount(count());
    }

    public void create(VoiceDrone voiceDrone) {
        if (voiceDroneRepository.existsById(voiceDrone.getChannelId())) throw new KeyAlreadyExistsException();
        voiceDroneRepository.save(voiceDrone);

        this.metrics.agentCreated();
        this.metrics.agentCount(count());
    }

    public void destroy(Long SnowflakeId) throws KeyNotFoundException {
        if (!voiceDroneRepository.existsById(SnowflakeId)) throw new KeyNotFoundException();
        voiceDroneRepository.deleteById(SnowflakeId);

        this.metrics.agentDestroyed();
        this.metrics.agentCount(count());
    }

    public void update(VoiceDrone drone) throws KeyNotFoundException {
        if (!voiceDroneRepository.existsById(drone.getChannelId())) throw new KeyNotFoundException();
        voiceDroneRepository.save(drone);

        this.metrics.agentUpdate();
    }

    public boolean exists(Long SnowflakeId) {
        return voiceDroneRepository.existsById(SnowflakeId);
    }

    public VoiceDrone find(Long snowflakeId) throws KeyNotFoundException {
        return voiceDroneRepository.findById(snowflakeId).orElseThrow(KeyNotFoundException::new);
    }

    public VoiceDrone findByChatId(Long snowflakeId) throws KeyNotFoundException {
        return voiceDroneRepository.findByChatId(snowflakeId);
    }

    public Long count() {
        return voiceDroneRepository.count();
    }

    public Page<VoiceDrone> findAll(Pageable request) {
        return voiceDroneRepository.findAll(request);
    }
}
