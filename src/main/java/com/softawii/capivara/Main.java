package com.softawii.capivara;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context         = SpringApplication.run(Main.class, args);
        JDA                            jda             = context.getBean(JDA.class);
        // BuildProperties                buildProperties = context.getBean(BuildProperties.class);
        String version = "unknown";
        jda.getPresence().setPresence(Activity.of(Activity.ActivityType.PLAYING, version), true);
        LOGGER.info(version + " Bot is ready as " + jda.getSelfUser().getName());
    }
}
