package com.softawii.capivara;

import com.softawii.capivara.core.EmbedManager;
import com.softawii.capivara.core.PackageManager;
import com.softawii.capivara.core.VoiceManager;
import com.softawii.capivara.listeners.EchoGroup;
import com.softawii.capivara.core.TemplateManager;
import com.softawii.capivara.listeners.PackageGroup;
import com.softawii.capivara.listeners.TemplateGroup;
import com.softawii.capivara.listeners.VoiceGroup;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Main implements CommandLineRunner {

    public static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        context = SpringApplication.run(Main.class, args);
        JDA jda = context.getBean(JDA.class);
        BuildProperties buildProperties = context.getBean(BuildProperties.class);
        jda.getPresence().setPresence(Activity.of(Activity.ActivityType.WATCHING,buildProperties.getVersion()), true);
        System.out.println(buildProperties.getVersion() + " Bot is ready as " + jda.getSelfUser().getName());

        // Beans
        PackageGroup.packageManager     = context.getBean(PackageManager.class);
        EchoGroup.embedManager          = context.getBean(EmbedManager.class);
        PackageGroup.embedManager       = EchoGroup.embedManager;
        TemplateGroup.templateManager   = context.getBean(TemplateManager.class);
        VoiceGroup.Dynamic.voiceManager = context.getBean(VoiceManager.class);
    }

    @Override
    public void run(String... args) {}
}
