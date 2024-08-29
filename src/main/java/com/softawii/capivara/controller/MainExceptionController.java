package com.softawii.capivara.controller;


import com.softawii.curupira.v2.annotations.DiscordException;
import com.softawii.curupira.v2.annotations.DiscordExceptions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.channel.GenericChannelEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@DiscordExceptions
public class MainExceptionController {

    private final Logger LOGGER = LogManager.getLogger(MainExceptionController.class);
    private       String channelId;
    private       Path   logDirectory;

    public MainExceptionController(String channelId, Path logDirectory) {
        this.channelId = channelId;
        this.logDirectory = logDirectory;
    }

    @DiscordException(Throwable.class)
    public void handle(Throwable throwable, Interaction interaction) {
        InputStream logFileBytes = null;
        if (logDirectory != null) {
            Path logFile = logDirectory.resolve("capivara.log");
            if (Files.isDirectory(logDirectory) && Files.exists(logFile) && Files.isRegularFile(logFile)) {
                try {
                    logFileBytes = Files.newInputStream(logFile);
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage(), e);
                }
            }
        }
        JDA         jda     = interaction.getJDA();
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel != null) {
            String       stackTrace         = getStackTrace(throwable);
            String       now                = OffsetDateTime.now(ZoneId.of("America/Sao_Paulo")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            String       stackTraceFileName = String.format("capivara-stacktrace-%s.log", now);
            String       logFileName        = String.format("capivara-log-%s.log", now);
            MessageEmbed interactionContext = getInteractionContext(interaction);
            try {
                MessageCreateAction messageAction = channel.sendFiles(FileUpload.fromData(stackTrace.getBytes(StandardCharsets.UTF_8), stackTraceFileName)).setEmbeds(interactionContext);
                if (logFileBytes != null) {
                    messageAction = messageAction.addFiles(FileUpload.fromData(logFileBytes, logFileName));
                }
                messageAction.submit();
            } catch (IllegalArgumentException e) {
                LOGGER.warn(e.getMessage(), e);
                channel.sendMessage(getStackTrace(e)).submit();
            }
        }
    }

    public void handle(Throwable throwable, Event event) {
        InputStream logFileBytes = null;
        if (logDirectory != null) {
            Path logFile = logDirectory.resolve("capivara.log");
            if (Files.isDirectory(logDirectory) && Files.exists(logFile) && Files.isRegularFile(logFile)) {
                try {
                    logFileBytes = Files.newInputStream(logFile);
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage(), e);
                }
            }
        }
        JDA         jda     = event.getJDA();
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel != null) {
            String       stackTrace         = getStackTrace(throwable);
            String       now                = OffsetDateTime.now(ZoneId.of("America/Sao_Paulo")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            String       stackTraceFileName = String.format("capivara-stacktrace-%s.log", now);
            String       logFileName        = String.format("capivara-log-%s.log", now);
            MessageEmbed interactionContext = getContext(event);
            try {
                MessageCreateAction messageAction = channel.sendFiles(FileUpload.fromData(stackTrace.getBytes(StandardCharsets.UTF_8), stackTraceFileName)).setEmbeds(interactionContext);
                if (logFileBytes != null) {
                    messageAction = messageAction.addFiles(FileUpload.fromData(logFileBytes, logFileName));
                }
                messageAction.submit();
            } catch (IllegalArgumentException e) {
                LOGGER.warn(e.getMessage(), e);
                channel.sendMessage(getStackTrace(e)).submit();
            }
        }
    }

    private MessageEmbed getContext(Event event) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.RED)
                .setTitle("Unhandled Exception");

        if (event instanceof GenericChannelEvent channelEvent) {
            if (channelEvent.isFromGuild()) {
                builder.addField("Guild ID", channelEvent.getGuild().getId(), true)
                        .addField("Guild Name", channelEvent.getGuild().getName(), true)
                        .addField("Guild Members", String.valueOf(channelEvent.getGuild().getMembers().size()), true);
            }
            builder.addField("Channel Type", channelEvent.getChannelType().toString(), true)
                    .addField("Channel ID", channelEvent.getChannel().getId(), true)
                    .addField("Channel Name", channelEvent.getChannel().getName(), true);
        } else if (event instanceof GenericGuildEvent guildEvent) {
            builder.addField("Guild ID", guildEvent.getGuild().getId(), true)
                    .addField("Guild Name", guildEvent.getGuild().getName(), true)
                    .addField("Guild Members", String.valueOf(guildEvent.getGuild().getMembers().size()), true);
        }

        return builder.build();
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter  printWriter  = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    private MessageEmbed getInteractionContext(Interaction interaction) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.RED)
                .setTitle("Unhandled Exception")
                .setFooter(interaction.getUser().getAsTag(), interaction.getUser().getEffectiveAvatarUrl())
                .setTimestamp(interaction.getTimeCreated());

        builder.addField("Channel Type", interaction.getChannelType().toString(), true)
                .addField("Interaction Type", interaction.getType().toString(), true)
                .addField("Interaction User", interaction.getUser().getAsTag(), true)
                .addField("Interaction User ID", interaction.getUser().getId(), true)
                .addField("Interaction User Locale", interaction.getUserLocale().getLocale(), true)
                .addField("Interaction Time Created", interaction.getTimeCreated().toString(), true);
        if (interaction.getChannel() != null) {
            builder.addField("Channel ID", interaction.getChannel().getId(), true)
                    .addField("Channel Name", interaction.getChannel().getName(), true);
        }
        if (interaction.getGuild() != null) {
            builder.addField("Guild ID", interaction.getGuild().getId(), true)
                    .addField("Guild Name", interaction.getGuild().getName(), true)
                    .addField("Guild Members", String.valueOf(interaction.getGuild().getMembers().size()), true);
        }

        return builder.build();
    }
}
