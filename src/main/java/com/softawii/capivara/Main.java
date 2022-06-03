package com.softawii.capivara;

import com.softawii.curupira.core.Curupira;
import com.softawii.capivara.core.PackageManager;
import com.softawii.capivara.listeners.PackageGroup;
import com.softawii.capivara.repository.PackageRepository;
import net.dv8tion.jda.api.JDA;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Main implements CommandLineRunner {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Main.class, args);
        JDA jda = context.getBean(JDA.class);
        System.out.println("Bot is ready as " + jda.getSelfUser().getName());
        Curupira curupira = context.getBean(Curupira.class);
        PackageManager pm = context.getBean(PackageManager.class);

        PackageGroup.packageManager = pm;

        PackageRepository repository = context.getBean(PackageRepository.class);
//        repository.findAll();
    }

    @Override
    public void run(String... args) {
        System.out.println("Nada");
    }
}
