package com.softawii.capivara.services;

import com.softawii.capivara.entity.DiscordMessage;
import com.softawii.capivara.repository.DiscordMessageRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class DiscordMessageService {
    private final Logger LOGGER = LogManager.getLogger(DiscordMessageService.class);
    private final DiscordMessageRepository repository;

    public DiscordMessageService(DiscordMessageRepository repository) {
        this.repository = repository;
    }

    public void save(DiscordMessage message) {
        LOGGER.info("Saving message: " + message.toString());
        repository.save(message);
    }

    public DiscordMessage find(Long id) {
        return repository.findById(id).orElse(null);
    }
}
