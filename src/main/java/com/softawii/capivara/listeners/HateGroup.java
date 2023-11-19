package com.softawii.capivara.listeners;

import com.softawii.capivara.core.DiscordMessageManager;
import com.softawii.curupira.annotations.ICommand;
import com.softawii.curupira.annotations.IGroup;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@IGroup(name="Hate", description = "Hate description", hidden = false)
@Component
public class HateGroup {

    private static DiscordMessageManager manager;

    @Autowired
    public void setDiscordMessageManager(DiscordMessageManager manager) {
        HateGroup.manager = manager;
    }

    @ICommand(name = "stats", description = "Hate overview in this server", permissions = {Permission.ADMINISTRATOR})
    public static void stats(SlashCommandInteractionEvent event) {

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
