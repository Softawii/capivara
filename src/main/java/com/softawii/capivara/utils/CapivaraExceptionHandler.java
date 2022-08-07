package com.softawii.capivara.utils;

import com.softawii.curupira.core.ExceptionHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.Interaction;

import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class CapivaraExceptionHandler implements ExceptionHandler {

    private String channelId;

    public CapivaraExceptionHandler(String channelId) {
        this.channelId = channelId;
    }

    @Override
    public void handle(Throwable throwable, Interaction interaction) {
        JDA         jda     = interaction.getJDA();
        TextChannel channel = jda.getTextChannelById(channelId);
        if (channel != null) {
            String       stackTrace         = getStackTrace(throwable);
            String       fileName           = String.format("capivara-%s.log", OffsetDateTime.now(ZoneId.of("America/Sao_Paulo")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            MessageEmbed interactionContext = getInteractionContext(interaction);
            try {
                channel.sendFile(stackTrace.getBytes(StandardCharsets.UTF_8), fileName)
                        .setEmbeds(interactionContext)
                        .queue();
            } catch (IllegalArgumentException e) {
                channel.sendMessage("Parece que o LOG é muito grande, vou diminuir pela metade").submit()
                        .thenCompose(message -> {
                            byte[] stackTraceHalfArray = Arrays.copyOfRange(stackTrace.getBytes(StandardCharsets.UTF_8), 0, stackTrace.getBytes(StandardCharsets.UTF_8).length / 2);
                            return channel.sendFile(stackTraceHalfArray, fileName)
                                    .setEmbeds(interactionContext)
                                    .submit();
                        }).whenComplete((message, t) -> {
                            if (e != null) {
                                e.printStackTrace();
                            }
                        });
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
