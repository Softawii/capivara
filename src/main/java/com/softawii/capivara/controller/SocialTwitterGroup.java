package com.softawii.capivara.controller;

import com.softawii.capivara.exceptions.MissingPermissionsException;
import com.softawii.capivara.services.TwitterParserConfigService;
import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.annotations.RequestInfo;
import com.softawii.curupira.v2.annotations.commands.DiscordCommand;
import com.softawii.curupira.v2.annotations.interactions.DiscordButton;
import com.softawii.curupira.v2.api.TextLocaleResponse;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

@Component
@DiscordController(parent = "social", value = "twitter", description = "Twitter Controller", permissions = Permission.ADMINISTRATOR,
        resource = "social", locales = DiscordLocale.PORTUGUESE_BRAZILIAN)
public class SocialTwitterGroup {
    public static final String deleteBotTwitterMessage = "twitter-bot-message-delete";
    private final TwitterParserConfigService service;

    public SocialTwitterGroup(TwitterParserConfigService service) {
        this.service = service;
    }

    public static Button generateDeleteButton(long authorId) {
        return Button.danger(String.format("%s:%s", deleteBotTwitterMessage, authorId), "Apagar");
    }

    @DiscordCommand(name = "enable", description = "Enable the automatic Twitter link transformation service")
    public TextLocaleResponse enable(Guild guild) {
        this.service.enable(guild.getIdLong());
        return new TextLocaleResponse("social.twitter.enable.response", guild.getName());
    }

    @DiscordCommand(name = "disable", description = "Disable the automatic Twitter link transformation service")
    public TextLocaleResponse disable(Guild guild) {
        this.service.disable(guild.getIdLong());
        return new TextLocaleResponse("social.twitter.disable.response", guild.getName());
    }

    @DiscordButton(name = deleteBotTwitterMessage, ephemeral = true)
    public TextLocaleResponse delete(ButtonInteractionEvent event, @RequestInfo Member member) throws MissingPermissionsException {
        // Format: ButtonID:Owner
        String ownerId = event.getComponentId().split(":")[1];
        String messageOwner = member.getId();

        MessageChannelUnion channel = event.getChannel();

        if (!messageOwner.equals(ownerId)) {
            throw new MissingPermissionsException();
        }

        channel.deleteMessageById(event.getMessageId()).queue();

        return new TextLocaleResponse("social.twitter.delete.response");
    }
}
