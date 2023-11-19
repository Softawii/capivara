package com.softawii.capivara.core;


import com.softawii.capivara.entity.DiscordMessage;
import com.softawii.capivara.services.DiscordMessageService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class DiscordMessageManager extends ListenerAdapter {
    private final Logger LOGGER = LogManager.getLogger(DiscordMessageManager.class);
    private final DiscordMessageService service;
    private JDA jda;

    public DiscordMessageManager(JDA jda, DiscordMessageService service) {
        this.jda = jda;
        this.service = service;
        this.jda.addEventListener(this);
        LOGGER.info("DiscordMessageManager initialized");
    }

    public void saveMessage(Message message) {
        DiscordMessage existingMessage = this.service.find(message.getIdLong());
        boolean isHate = false;
        boolean isHateOpenai = false;
        boolean checked = false;
        if(existingMessage != null) {
            isHate = existingMessage.isHate();
            isHateOpenai = existingMessage.isHateOpenai();
            checked = existingMessage.isChecked();
        }

        this.service.save(new DiscordMessage(message, checked, isHate, isHateOpenai));
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if(event.getAuthor().isBot()) return;
        if(!event.isFromGuild()) return;

        saveMessage(event.getMessage());
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        if(event.getAuthor().isBot()) return;
        if(!event.isFromGuild()) return;

        saveMessage(event.getMessage());
    }
}
