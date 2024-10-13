package com.softawii.capivara.controller;

import com.softawii.capivara.exceptions.MissingPermissionsException;
import com.softawii.capivara.services.SocialParserConfigService;
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
public class SocialTwitterController {
    private final SocialParserConfigService service;

    public SocialTwitterController(SocialParserConfigService service) {
        this.service = service;
    }

    @DiscordCommand(name = "enable", description = "Enable the automatic Twitter link transformation service")
    public TextLocaleResponse enable(Guild guild) {
        this.service.changeTwitter(guild.getIdLong(), true);
        return new TextLocaleResponse("social.twitter.enable.response", guild.getName());
    }

    @DiscordCommand(name = "disable", description = "Disable the automatic Twitter link transformation service")
    public TextLocaleResponse disable(Guild guild) {
        this.service.changeTwitter(guild.getIdLong(), false);
        return new TextLocaleResponse("social.twitter.disable.response", guild.getName());
    }
}
