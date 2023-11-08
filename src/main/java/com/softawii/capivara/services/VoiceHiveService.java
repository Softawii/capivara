package com.softawii.capivara.services;

import com.softawii.capivara.entity.VoiceHive;
import com.softawii.capivara.exceptions.ExistingDynamicCategoryException;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.repository.VoiceHiveRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

@Singleton
public class VoiceHiveService {

    private final VoiceHiveRepository voiceHiveRepository;

    public VoiceHiveService(VoiceHiveRepository voiceHiveRepository) {
        this.voiceHiveRepository = voiceHiveRepository;
    }

    public VoiceHive create(VoiceHive voiceHive) throws ExistingDynamicCategoryException {
        if (voiceHiveRepository.existsById(voiceHive.getCategoryId())) throw new ExistingDynamicCategoryException();
        return voiceHiveRepository.save(voiceHive);
    }

    public void destroy(Long SnowflakeId) throws KeyNotFoundException {
        Optional<VoiceHive> voiceHive = voiceHiveRepository.findById(SnowflakeId);
        if (voiceHive.isEmpty()) throw new KeyNotFoundException();

        // voiceHiveRepository
        voiceHiveRepository.deleteById(SnowflakeId);
    }

    public List<VoiceHive> findAllByGuildId(Long SnowflakeId) {
        return voiceHiveRepository.findAllByGuildId(SnowflakeId);
    }

    public VoiceHive update(VoiceHive voiceHive) throws KeyNotFoundException {
        if (!voiceHiveRepository.existsById(voiceHive.getCategoryId())) throw new KeyNotFoundException();
        return voiceHiveRepository.update(voiceHive);
    }

    public boolean existsByCategoryId(long SnowflakeId) {
        return voiceHiveRepository.existsByCategoryId(SnowflakeId);
    }

    public VoiceHive find(Long SnowflakeId) throws KeyNotFoundException {
        Optional<VoiceHive> voiceHive = voiceHiveRepository.findById(SnowflakeId);
        return voiceHive.orElseThrow(KeyNotFoundException::new);
    }

    public Page<VoiceHive> findAll(Pageable request) {
        return voiceHiveRepository.findAll(request);
    }
}
