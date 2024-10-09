package com.softawii.capivara.controller;

import com.softawii.capivara.exceptions.MissingPermissionsException;
import com.softawii.curupira.v2.annotations.DiscordController;
import com.softawii.curupira.v2.annotations.RequestInfo;
import com.softawii.curupira.v2.annotations.interactions.DiscordButton;
import com.softawii.curupira.v2.api.TextLocaleResponse;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.DiscordLocale;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;

@Component
@DiscordController(parent = "social", value = "generic", description = "Social Generic Controller", permissions = Permission.ADMINISTRATOR, resource = "social", locales = DiscordLocale.PORTUGUESE_BRAZILIAN)
public class SocialGenericController {
    public static final String deleteBotMessage = "social-bot-message-delete";

    public static Button generateDeleteButton(long authorId) {
        return Button.danger(String.format("%s:%s", deleteBotMessage, authorId), "Delete");
    }

    @DiscordButton(name = deleteBotMessage, ephemeral = true)
    public TextLocaleResponse delete(ButtonInteractionEvent event, @RequestInfo Member member) throws MissingPermissionsException {
        // Format: ButtonID:Owner
        String ownerId = event.getComponentId().split(":")[1];
        String messageOwner = member.getId();

        MessageChannelUnion channel = event.getChannel();

        if (!messageOwner.equals(ownerId)) {
            throw new MissingPermissionsException();
        }

        channel.deleteMessageById(event.getMessageId()).queue();

        return new TextLocaleResponse("social.delete.response");
    }
}
