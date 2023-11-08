package com.softawii.capivara.config;

import com.softawii.capivara.utils.CapivaraExceptionHandler;
import com.softawii.curupira.core.Curupira;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.nio.file.Path;

@Factory
public class AppFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppFactory.class);

    @Bean
    @Context
    public JDA jda(@Value("${token}") String discordToken) {
        JDA jda;
        try {
            JDABuilder builder = JDABuilder.create(discordToken, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.GUILD_PRESENCES);
            builder.setMemberCachePolicy(MemberCachePolicy.ALL);
            builder.enableCache(CacheFlag.EMOJI, CacheFlag.ROLE_TAGS, CacheFlag.MEMBER_OVERRIDES, CacheFlag.STICKER);
            jda = builder.build();
            jda.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return jda;
    }

    @Bean
    @Context
    public Curupira curupira(
            JDA jda,
            @Value("${curupira.reset:false}") Boolean reset,
            @Value("${log.channel.id}") @Nullable String logChannelId,
            @Value("${log_directory}") @Nullable String logDirectory
    ) {
        String pkg = "com.softawii.capivara.listeners";
        LOGGER.info("curupira.reset: " + reset);

        CapivaraExceptionHandler exceptionHandler = null;
        if (logChannelId != null) {
            Path logPath = null;
            if (logDirectory != null) {
                logPath = Path.of(logDirectory);
            }
            exceptionHandler = new CapivaraExceptionHandler(logChannelId, logPath);
        }
        return new Curupira(jda, reset, exceptionHandler, pkg);
    }
}
