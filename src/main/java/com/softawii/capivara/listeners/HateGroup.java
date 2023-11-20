package com.softawii.capivara.listeners;

import com.softawii.capivara.core.DiscordMessageManager;
import com.softawii.capivara.exceptions.FieldLengthException;
import com.softawii.capivara.services.DiscordMessageService;
import com.softawii.curupira.annotations.IArgument;
import com.softawii.curupira.annotations.IButton;
import com.softawii.curupira.annotations.ICommand;
import com.softawii.curupira.annotations.IGroup;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
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
    @IArgument(name = "user",
            description = "User to kick",
            required = true, type = OptionType.USER)
    public static void user(SlashCommandInteractionEvent event) {
        User evaluate = event.getOption("user").getAsUser();
        try {
            manager.getStatsByGuildIdAndUserId(event, event.getGuild().getIdLong(), evaluate.getIdLong(), 0);
        } catch (FieldLengthException e) {
            event.reply("Error: " + e.getMessage()).setEphemeral(true).queue();
        }
    }

    @ICommand(name = "global", description = "Hate overview", permissions = {Permission.ADMINISTRATOR})
    public static void global(SlashCommandInteractionEvent event) {
        Long userId = event.getUser().getIdLong();
        Long[] owners = new Long[] { 232906166059925506L, 284341657531449344L, 366342872892440586L };

        if(!Arrays.asList(owners).contains(userId)) {
            event.reply("You are not allowed to use this command.").setEphemeral(true).queue();
            return;
        }

        try {
            MessageEmbed embed = manager.getGlobalStats();
            event.replyEmbeds(embed).setEphemeral(false).queue();
        } catch (FieldLengthException e) {
            event.reply("Error: " + e.getMessage()).setEphemeral(true).queue();
        }
    }

    @IButton(id="hate-stats")
    public static void statsButton(ButtonInteractionEvent event) {
        String eventId = event.getComponentId();
        String[] split = eventId.split(":");
        Long guildId = Long.parseLong(split[1]);
        Long userId = Long.parseLong(split[2]);
        int page = Integer.parseInt(split[3]);

        try {
            manager.editStatsByGuildIdAndUserId(event, guildId, userId, page);
        } catch (FieldLengthException e) {
            event.reply("Error: " + e.getMessage()).setEphemeral(true).queue();
        }
    }
}
