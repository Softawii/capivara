package com.softawii.capivara.services;

import com.softawii.capivara.entity.VoiceHive;
import com.softawii.capivara.exceptions.ExistingDynamicCategoryException;
import com.softawii.capivara.exceptions.KeyAlreadyInPackageException;
import com.softawii.capivara.exceptions.KeyNotFoundException;
import com.softawii.capivara.repository.VoiceHiveRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VoiceHiveService {

    private final VoiceHiveRepository voiceHiveRepository;

    public VoiceHiveService(VoiceHiveRepository voiceHiveRepository) {
        this.voiceHiveRepository = voiceHiveRepository;
    }

    public VoiceHive create(VoiceHive voiceHive) throws ExistingDynamicCategoryException {
        if (voiceHiveRepository.existsByHiveKey(voiceHive.hiveKey())) throw new ExistingDynamicCategoryException();
        return voiceHiveRepository.save(voiceHive);
    }

    public void destroy(Long SnowflakeId) throws KeyNotFoundException {
        Optional<VoiceHive> voiceHive = voiceHiveRepository.findByHiveKey_CategoryId(SnowflakeId);
        if (!voiceHive.isPresent()) throw new KeyNotFoundException();

        // voiceHiveRepository
        voiceHiveRepository.deleteByHiveKey_CategoryId(SnowflakeId);
    }

    public List<VoiceHive> findAllByGuildId(Long SnowflakeId) {
        return voiceHiveRepository.findAllByHiveKey_GuildId(SnowflakeId);
    }


    public VoiceHive update(VoiceHive voiceHive) throws KeyNotFoundException {
        if (voiceHiveRepository.existsByHiveKey(voiceHive.hiveKey())) throw new KeyNotFoundException();
        return voiceHiveRepository.save(voiceHive);
    }

    public boolean existsByCategoryId(long SnowflakeId) {
        return voiceHiveRepository.existsByHiveKey_CategoryId(SnowflakeId);
    }

    public VoiceHive find(Long SnowflakeId) throws KeyNotFoundException {
        Optional<VoiceHive> voiceHive = voiceHiveRepository.findByHiveKey_CategoryId(SnowflakeId);
        return voiceHive.orElseThrow(KeyNotFoundException::new);
    }
}
