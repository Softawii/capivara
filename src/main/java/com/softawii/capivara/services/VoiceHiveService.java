package com.softawii.capivara.services;

import com.softawii.capivara.entity.VoiceHive;
import com.softawii.capivara.exceptions.ExistingDynamicCategoryException;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.metrics.VoiceMetrics;
import com.softawii.capivara.repository.VoiceHiveRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VoiceHiveService {

    private final VoiceHiveRepository voiceHiveRepository;
    private final VoiceMetrics metrics;

    public VoiceHiveService(VoiceHiveRepository voiceHiveRepository, VoiceMetrics metrics) {
        this.voiceHiveRepository = voiceHiveRepository;
        this.metrics = metrics;
        this.metrics.masterCount(count());
    }

    public VoiceHive create(VoiceHive voiceHive) throws ExistingDynamicCategoryException {
        if (voiceHiveRepository.existsById(voiceHive.getCategoryId())) throw new ExistingDynamicCategoryException();
        VoiceHive hive = voiceHiveRepository.save(voiceHive);

        this.metrics.masterCreated();
        this.metrics.masterCount(count());

        return hive;
    }

    public void destroy(Long SnowflakeId) throws KeyNotFoundException {
        Optional<VoiceHive> voiceHive = voiceHiveRepository.findById(SnowflakeId);
        if (voiceHive.isEmpty()) throw new KeyNotFoundException();
        voiceHiveRepository.deleteById(SnowflakeId);

        this.metrics.masterDestroyed();
        this.metrics.masterCount(count());
    }

    public Long count() {
        return this.voiceHiveRepository.count();
    }

    public List<VoiceHive> findAllByGuildId(Long SnowflakeId) {
        return voiceHiveRepository.findAllByGuildId(SnowflakeId);
    }

    public VoiceHive update(VoiceHive voiceHive) throws KeyNotFoundException {
        if (!voiceHiveRepository.existsById(voiceHive.getCategoryId())) throw new KeyNotFoundException();
        this.metrics.masterUpdate();
        return voiceHiveRepository.save(voiceHive);
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
