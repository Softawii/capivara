package com.softawii.capivara.services;

import com.softawii.capivara.entity.VoiceDrone;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.repository.VoiceDroneRepository;
import org.springframework.stereotype.Service;

import javax.management.openmbean.KeyAlreadyExistsException;

@Service
public class VoiceDroneService {

    private final VoiceDroneRepository voiceDroneRepository;

    public VoiceDroneService(VoiceDroneRepository voiceDroneRepository) {
        this.voiceDroneRepository = voiceDroneRepository;
    }

    public VoiceDrone create(VoiceDrone voiceDrone) {
        if (voiceDroneRepository.existsById(voiceDrone.getChannelId())) throw new KeyAlreadyExistsException();
        return voiceDroneRepository.save(voiceDrone);
    }

    public void destroy(Long SnowflakeId) throws KeyNotFoundException {
        if (!voiceDroneRepository.existsById(SnowflakeId)) throw new KeyNotFoundException();
        voiceDroneRepository.deleteById(SnowflakeId);
    }

    public void update(VoiceDrone drone) throws KeyNotFoundException {
        if (!voiceDroneRepository.existsById(drone.getChannelId())) throw new KeyNotFoundException();
        voiceDroneRepository.save(drone);
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
}
