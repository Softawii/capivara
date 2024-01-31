package com.softawii.capivara;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context         = SpringApplication.run(Main.class, args);
        JDA                            jda             = context.getBean(JDA.class);
        BuildProperties                buildProperties = context.getBean(BuildProperties.class);
        jda.getPresence().setPresence(Activity.of(Activity.ActivityType.PLAYING, buildProperties.getVersion()), true);
        LOGGER.info(buildProperties.getVersion() + " Bot is ready as " + jda.getSelfUser().getName());
    }
}
