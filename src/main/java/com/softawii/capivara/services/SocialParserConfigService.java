package com.softawii.capivara.services;

import com.softawii.capivara.entity.SocialParserConfig;
import com.softawii.capivara.repository.SocialParserConfigRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SocialParserConfigService {

    private final SocialParserConfigRepository repository;

    public SocialParserConfigService(SocialParserConfigRepository repository) {
        this.repository = repository;
    }

    public boolean isTwitterEnabled(Long guildId) {
        Optional<SocialParserConfig> config = repository.findById(guildId);
        return config.map(SocialParserConfig::isTwitter).orElse(false);
    }

    public boolean isBskyEnabled(Long guildId) {
        Optional<SocialParserConfig> config = repository.findById(guildId);
        return config.map(SocialParserConfig::isBsky).orElse(false);
    }

    public void changeTwitter(Long guildId, boolean enable) {
        Optional<SocialParserConfig> optional = repository.findById(guildId);
        SocialParserConfig config = optional.orElseGet(() -> new SocialParserConfig(guildId));
        config.setTwitter(enable);

        if(config.isTwitter() || config.isBsky()) {
            repository.save(config);
        }
        else if(optional.isPresent()) {
            repository.deleteById(guildId);
        }
    }

    public void changeBluesky(Long guildId, boolean enable) {
        Optional<SocialParserConfig> optional = repository.findById(guildId);
        SocialParserConfig config = optional.orElseGet(() -> new SocialParserConfig(guildId));
        config.setBsky(enable);

        if(config.isTwitter() || config.isBsky()) {
            repository.save(config);
        }
        else if(optional.isPresent()) {
            repository.deleteById(guildId);
        }
    }
}
