package com.softawii.capivara.core;


import com.softawii.capivara.entity.DiscordMessage;
import com.softawii.capivara.entity.HateGuild;
import com.softawii.capivara.entity.HateStats;
import com.softawii.capivara.entity.HateUser;
import com.softawii.capivara.exceptions.FieldLengthException;
import com.softawii.capivara.services.DiscordMessageService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
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
        DiscordMessage discordMessage = new DiscordMessage(message, checked, isHate, isHateOpenai);
        if(!discordMessage.getContent().isEmpty()) this.service.save(discordMessage);
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

        List<HateUser> haters = service.getMostHatedUsersByGuildId(guildId, 5);

        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= haters.size(); i++) {
            HateUser user = haters.get(i - 1);
            sb.append(i).append(". ").append(user.getUser().getAsMention()).append(" - ").append(user.getHateCount()).append(" hate messages\n");
        }

        handler.addField(new MessageEmbed.Field("Top 5 users with more hate messages", sb.toString(), false));
        handler.getBuilder().setColor(new Color(200, 72, 63));

        return handler.build();
    }

    public void getStatsByGuildIdAndUserId(SlashCommandInteractionEvent event, Long guildId, Long userId, int page) throws FieldLengthException {
        List<DiscordMessage> messages = getDiscordMessagesByPage(guildId, userId, page);
        EmbedManager.EmbedHandler handler = generateHandlerToHateUser(guildId, userId, page, messages);

        event.replyEmbeds(handler.build()).addActionRow(
                Button.primary("hate-stats:" + guildId + ":" + userId + ":" + (page - 1), "Previous page").withDisabled(page == 0),
                Button.primary("hate-stats:" + guildId + ":" + userId + ":" + (page + 1), "Next page").withDisabled(messages.size() < 5)
        ).setEphemeral(true).queue();
    }

    public void editStatsByGuildIdAndUserId(ButtonInteractionEvent event, Long guildId, Long userId, int page) throws FieldLengthException {
        List<DiscordMessage> messages = getDiscordMessagesByPage(guildId, userId, page);
        EmbedManager.EmbedHandler handler = generateHandlerToHateUser(guildId, userId, page, messages);

        event.editMessageEmbeds(handler.build()).setActionRow(
                Button.primary("hate-stats:" + guildId + ":" + userId + ":" + (page - 1), "Previous page").withDisabled(page == 0),
                Button.primary("hate-stats:" + guildId + ":" + userId + ":" + (page + 1), "Next page").withDisabled(messages.size() < 5)
        ).queue();
    }

    public EmbedManager.EmbedHandler generateHandlerToHateUser(Long guildId, Long userId, int page, List<DiscordMessage> messages) throws FieldLengthException {
        HateUser user = service.getHateStatsByGuildIdAndUserId(guildId, userId);
        EmbedManager.EmbedHandler handler = new EmbedManager.EmbedHandler();

        if(user != null) {
            handler.setTitle("Stats of " + user.getUser().getName());
            handler.setDescription("Hate stats of this user");
            handler.addField(new MessageEmbed.Field("Total evaluated messages", user.getMessageCount().toString(), false));
            handler.addField(new MessageEmbed.Field("Total hate messages", user.getHateCount().toString(), false));
            handler.addField(new MessageEmbed.Field("Hate percentage", String.format("%.2f", user.getHate()) + "%", false));
            handler.getBuilder().setColor(new Color(243, 60, 99));
        } else {
            handler.setTitle("User not found");
            handler.setDescription("This user has no hate messages");
            handler.getBuilder().setColor(new Color(243, 60, 99));
        }

        StringBuilder sb = new StringBuilder();
        for(DiscordMessage message : messages) {
            sb.append(message.isHate() ? "🤬" : "😀").append(" ").append(message.getContent()).append("\n");
        }

        handler.addField(new MessageEmbed.Field("Messages", sb.toString(), false));
        handler.addField(new MessageEmbed.Field("Page", String.valueOf(page + 1), false));

        return handler;
    }

    public List<DiscordMessage> getDiscordMessagesByPage(Long guildId, Long userId, int page) {
        Pageable pageable = Pageable.ofSize(5).withPage(page);
        return service.getDiscordMessageByGuildIdAndUserIdAndCheckedIsTrue(guildId, userId, pageable).toList();
    }

    public MessageEmbed getGlobalStats() throws FieldLengthException {
        HateStats stats = service.getGlobalStats();

        EmbedManager.EmbedHandler handler = new EmbedManager.EmbedHandler();
        handler.setTitle("Global stats");
        handler.setDescription("Hate stats of all servers");
        handler.addField(new MessageEmbed.Field("Total evaluated messages", stats.getMessageCount().toString(), false));
        handler.addField(new MessageEmbed.Field("Total hate messages", stats.getHateCount().toString(), false));
        handler.addField(new MessageEmbed.Field("Hate percentage", String.format("%.2f", stats.getHate()) + "%", false));
        handler.getBuilder().setColor(new Color(200, 72, 63));

        List<HateGuild> haters = service.getMostHatefulGuilds();

        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <= haters.size(); i++) {
            HateGuild guild = haters.get(i - 1);
            sb.append(i).append(". ").append(guild.getGuild().getName()).append(" - ").append(guild.getStats().getHateCount()).append(" hate messages\n");
        }

        handler.addField(new MessageEmbed.Field("Top 10 servers with more hate messages", sb.toString(), false));

        return handler.build();
    }
}
