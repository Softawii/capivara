package com.softawii.capivara.services;

import com.softawii.capivara.entity.DiscordMessage;
import com.softawii.capivara.entity.HateStats;
import com.softawii.capivara.repository.DiscordMessageRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.persistence.Tuple;
import java.math.BigInteger;

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

    public HateStats statsByServer(Long guildId) {
        Tuple stats = repository.getHateStatsByGuildId(guildId);
        BigInteger messageCount = stats.get("MESSAGECOUNT", BigInteger.class);
        BigInteger hateCount = stats.get("HATECOUNT", BigInteger.class);
        Double hate = stats.get("HATE", Double.class);

        return new HateStats(messageCount, hateCount, hate);
    }
}
