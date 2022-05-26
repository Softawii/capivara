package com.softwaii.laputa;

import com.softawii.curupira.core.Curupira;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class Main {

    public static void main(String[] args) throws LoginException, InterruptedException {
        String token = "OTM5OTcyODU1MDg3NjUyOTM0.G_6Z61.L1P-ugR8_yyr72LKUS3KUlW-VnYn_ovSyxuEhI";
        String pkg   = "com.softwaii.laputa.listeners";

        // Default Builder
        // We Will Build with Listeners and Slash Commands
        JDABuilder builder = JDABuilder.createDefault(token);
        JDA JDA = builder.build();

        Curupira curupira = new Curupira(JDA, pkg);

        JDA.awaitReady();
        System.out.println("Bot is ready as " + JDA.getSelfUser().getName());
    }
}
