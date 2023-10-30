package com.softawii.capivara.listeners;

import com.softawii.curupira.annotations.IArgument;
import com.softawii.curupira.annotations.ICommand;
import com.softawii.curupira.annotations.IGroup;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@IGroup(name = "calendar", description = "Calendar events", hidden = false)
public class CalendarGroup {

    @ICommand(name = "subscribe", description = "Subscribe to a Google Calendar", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name = "calendar-id", description = "The ID of the calendar to subscribe to", required = true)
    @IArgument(name = "name", description = "The name to use for the calendar", required = true)
    @IArgument(name = "channel", description = "The channel where events will be announced", required = true, type = OptionType.CHANNEL)
    @IArgument(name = "role", description = "The role to be pinged when events begin", required = false, type = OptionType.ROLE)
    public static void subscribe(SlashCommandInteractionEvent event) {
        event.reply("Not implemented yet").queue();
    }

    @ICommand(name = "unsubscribe", description = "Unsubscribe from a Google Calendar", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name = "name", description = "The name to use for the calendar", required = true)
    public static void unsubscribe(SlashCommandInteractionEvent event) {
        event.reply("Not implemented yet").queue();
    }

    @ICommand(name = "list", description = "List all events from a Google Calendar")
    @IArgument(name = "name", description = "The name to use for the calendar", required = false)
    public static void list(SlashCommandInteractionEvent event) {
        event.reply("Not implemented yet").queue();
    }

    @ICommand(name = "update", description = "Update configuration of a calendar", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name = "name", description = "The name to use for the calendar", required = true)
    @IArgument(name = "channel", description = "The channel where events will be announced", required = true, type = OptionType.CHANNEL)
    @IArgument(name = "role", description = "The role to be pinged when events begin", required = false, type = OptionType.ROLE)
    public static void update(SlashCommandInteractionEvent event) {
        event.reply("Not implemented yet").queue();
    }
}
