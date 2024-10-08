package com.softawii.capivara.services;

import com.softawii.capivara.entity.TwitterParserConfig;
import com.softawii.capivara.repository.TwitterParserConfigRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

@Service
public class TwitterParserConfigService {

    private final TwitterParserConfigRepository repository;

    public TwitterParserConfigService(TwitterParserConfigRepository repository) {
        this.repository = repository;
    }

    public boolean isEnabled(Long guildId) {
        return repository.existsById(guildId);
    }

    public void enable(Long guildId) {
        repository.save(new TwitterParserConfig(guildId));
    }

    public void disable(Long guildId) {
        try {
            repository.deleteById(guildId);
        } catch(EmptyResultDataAccessException e) {
            // Nothing
        }
    }
}
