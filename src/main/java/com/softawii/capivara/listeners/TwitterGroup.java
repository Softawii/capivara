package com.softawii.capivara.listeners;

import com.softawii.capivara.services.TwitterTransformService;
import com.softawii.curupira.annotations.ICommand;
import com.softawii.curupira.annotations.IGroup;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@IGroup(name = "twitter", description = "Transformação de links do Twitter automática", hidden = false)
@Component
public class TwitterGroup {

    private static TwitterTransformService service;

    @Autowired
    public void setCurupira(TwitterTransformService service) {
        TwitterGroup.service = service;
    }

    @ICommand(name = "enable", description = "Ativa o serviço de transformação de links do Twitter automática", permissions = {Permission.ADMINISTRATOR})
    public static void enable(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        if (service.isEnabled(guildId)) {
            event.reply("O serviço já está ativado").setEphemeral(true).queue();
            return;
        }

        service.enable(guildId);
        event.reply("O serviço foi está ativado").setEphemeral(true).queue();
    }

    @ICommand(name = "disable", description = "Desativa o serviço de transformação de links do Twitter automática", permissions = {Permission.ADMINISTRATOR})
    public static void disable(SlashCommandInteractionEvent event) {
        long guildId = event.getGuild().getIdLong();
        if (!service.isEnabled(guildId)) {
            event.reply("O serviço não está ativado").setEphemeral(true).queue();
            return;
        }

        service.disable(guildId);
        event.reply("O serviço foi está ativado").setEphemeral(true).queue();
    }
}
