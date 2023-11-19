package com.softawii.capivara.core;


import com.softawii.capivara.entity.DiscordMessage;
import com.softawii.capivara.entity.HateStats;
import com.softawii.capivara.entity.HateUser;
import com.softawii.capivara.exceptions.FieldLengthException;
import com.softawii.capivara.services.DiscordMessageService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.List;

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

    public MessageEmbed getStatsByGuildId(Long guildId) throws FieldLengthException {
        HateStats stats = service.statsByServer(guildId);

        EmbedManager.EmbedHandler handler = new EmbedManager.EmbedHandler();
        handler.setTitle("Stats");
        handler.setDescription("Hate stats of this server");
        handler.addField(new MessageEmbed.Field("Total evaluated messages", stats.getMessageCount().toString(), false));
        handler.addField(new MessageEmbed.Field("Total hate messages", stats.getHateCount().toString(), false));
        handler.addField(new MessageEmbed.Field("Hate percentage", String.format("%.2f", stats.getHate()) + "%", false));

        // TODO: Add Top 5 users with more hate
        List<HateUser> haters = service.getMostHatedUsersByGuildId(guildId, 5);

        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= haters.size(); i++) {
            HateUser user = haters.get(i - 1);
            sb.append(i).append(". ").append(user.getUser().getAsMention()).append(" - ").append(String.format("%.2f", user.getHate())).append("%\n");
        }

        handler.addField(new MessageEmbed.Field("Top 5 users with more hate", sb.toString(), false));
        handler.getBuilder().setColor(new Color(200, 72, 63));

        return handler.build();
    }
}
