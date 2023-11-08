package com.softawii.capivara.utils;

import com.softawii.curupira.core.ExceptionHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class CapivaraExceptionHandler implements ExceptionHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(CapivaraExceptionHandler.class);
    private       String channelId;
    private       Path   logDirectory;

    public CapivaraExceptionHandler(String channelId, Path logDirectory) {
        this.channelId = channelId;
        this.logDirectory = logDirectory;
    }

    @Override
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
