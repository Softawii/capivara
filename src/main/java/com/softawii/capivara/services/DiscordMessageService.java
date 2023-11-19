package com.softawii.capivara.services;

import com.softawii.capivara.entity.DiscordMessage;
import com.softawii.capivara.entity.HateStats;
import com.softawii.capivara.entity.HateUser;
import com.softawii.capivara.repository.DiscordMessageRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.persistence.Tuple;
import java.math.BigInteger;
import java.util.List;

@Service
public class DiscordMessageService {
    private final Logger LOGGER = LogManager.getLogger(DiscordMessageService.class);
    private final DiscordMessageRepository repository;
    private final JDA jda;

    public DiscordMessageService(JDA jda, DiscordMessageRepository repository) {
        this.repository = repository;
        this.jda = jda;
    }

    public void save(DiscordMessage message) {
        LOGGER.info("Saving message: " + message.toString());
        repository.save(message);
    }

    public DiscordMessage find(Long id) {
        return repository.findById(id).orElse(null);
    }

    public HateStats statsByServer(Long guildId) {
        BigInteger messageCount = BigInteger.valueOf(0);
        BigInteger hateCount = BigInteger.valueOf(0);
        Double hate = 0.0;

        Tuple stats = repository.getHateStatsByGuildId(guildId);

        if (stats != null) {
            messageCount = stats.get("MESSAGECOUNT", BigInteger.class);
            hateCount = stats.get("HATECOUNT", BigInteger.class);
            hate = stats.get("HATE", Double.class);
        }

        return new HateStats(messageCount, hateCount, hate);
    }

    public List<HateUser> getMostHatedUsersByGuildId(Long guildId, Integer limit) {
        List<Tuple> users = repository.getMostHatedUsersByGuildId(guildId, limit);

        return users.stream().map((user) -> {
           BigInteger userId = user.get("USERID", BigInteger.class);
           BigInteger messageCount = user.get("MESSAGECOUNT", BigInteger.class);
           BigInteger hateCount = user.get("HATECOUNT", BigInteger.class);
           Double hate = user.get("HATE", Double.class);

           User discordUserEntity = this.jda.getUserById(userId.longValue());

           return new HateUser(discordUserEntity, messageCount, hateCount, hate);
       }).toList();
    }
}
