package com.softawii.capivara.listeners;

import com.softawii.capivara.core.DiscordMessageManager;
import com.softawii.capivara.exceptions.FieldLengthException;
import com.softawii.capivara.services.DiscordMessageService;
import com.softawii.curupira.annotations.ICommand;
import com.softawii.curupira.annotations.IGroup;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@IGroup(name="Hate", description = "Hate description", hidden = false)
@Component
public class HateGroup {

    private static DiscordMessageManager manager;
    private static DiscordMessageService service;

    @Autowired
    public void setDiscordMessageManager(DiscordMessageManager manager) {
        HateGroup.manager = manager;
    }

    @Autowired
    public void setDiscordMessageService(DiscordMessageService service) {
        HateGroup.service = service;
    }

    @ICommand(name = "stats", description = "Hate overview in this server", permissions = {Permission.ADMINISTRATOR})
    public static void stats(SlashCommandInteractionEvent event) {
        try {
            MessageEmbed embed = manager.getStatsByGuildId(event.getGuild().getIdLong());
            event.replyEmbeds(embed).setEphemeral(false).queue();
        } catch (FieldLengthException e) {
            event.reply("Error: " + e.getMessage()).setEphemeral(true).queue();
        }
    }

    @ICommand(name = "user", description = "Hate overview by user", permissions = {Permission.ADMINISTRATOR})
    public static void user(SlashCommandInteractionEvent event) {

    }

    @ICommand(name = "global", description = "Hate overview", permissions = {Permission.ADMINISTRATOR})
    public static void global(SlashCommandInteractionEvent event) {
        Long userId = event.getUser().getIdLong();
        Long[] owners = new Long[] { 232906166059925506L, 284341657531449344L, 366342872892440586L };

        if(!Arrays.asList(owners).contains(userId)) {
            event.reply("You are not allowed to use this command.").setEphemeral(true).queue();
        }
    }
}
