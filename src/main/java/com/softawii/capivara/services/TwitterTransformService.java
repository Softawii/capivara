package com.softawii.capivara.services;

import com.softawii.capivara.entity.TwitterTransform;
import com.softawii.capivara.repository.TwitterTransformRepository;
import org.springframework.stereotype.Service;

@Service
public class TwitterTransformService {

    private final TwitterTransformRepository repository;

    public TwitterTransformService(TwitterTransformRepository repository) {
        this.repository = repository;
    }

    public boolean isEnabled(Long guildId) {
        return repository.existsById(guildId);
    }

    public void enable(Long guildId) {
        repository.save(new TwitterTransform(guildId));
    }

    public void disable(Long guildId) {
        repository.deleteById(guildId);
    }
}
