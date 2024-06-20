package com.softawii.capivara.listeners;

import com.softawii.capivara.core.CalendarManager;
import com.softawii.capivara.exceptions.DuplicatedKeyException;
import com.softawii.curupira.annotations.IArgument;
import com.softawii.curupira.annotations.ICommand;
import com.softawii.curupira.annotations.IGroup;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@IGroup(name = "calendar", description = "Calendar events", hidden = false)
@Component
public class CalendarGroup {

    private static CalendarManager calendarManager;

    @Autowired
    public void setCalendarManager(CalendarManager calendarManager) {
        CalendarGroup.calendarManager = calendarManager;
    }

    @ICommand(name = "subscribe", description = "Subscribe to a Google Calendar", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name = "calendar-id", description = "The ID of the calendar to subscribe to", required = true)
    @IArgument(name = "name", description = "The name to use for the calendar", required = true)
    @IArgument(name = "channel", description = "The channel where events will be announced", required = true, type = OptionType.CHANNEL)
    @IArgument(name = "role", description = "The role to be pinged when events begin", required = false, type = OptionType.ROLE)
    public static void subscribe(SlashCommandInteractionEvent event) {
        String            googleCalendarId = event.getOption("calendar-id").getAsString();
        String            name             = event.getOption("name").getAsString();
        GuildChannelUnion channel          = event.getOption("channel").getAsChannel();
        Role              role             = event.getOption("role") != null ? event.getOption("role").getAsRole() : null;
        try {
            calendarManager.createCalendar(googleCalendarId, name, channel, role);
        } catch (DuplicatedKeyException e) {
            event.reply("Calendar already exists with this name and guild, use update instead").queue();
            return;
        }
        event.reply("event subscribed").queue();
    }

    @ICommand(name = "unsubscribe", description = "Unsubscribe from a Google Calendar", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name = "name", description = "The name to use for the calendar", required = true)
    public static void unsubscribe(SlashCommandInteractionEvent event) {
        event.reply("Not implemented yet").queue();
    }

    @ICommand(name = "list", description = "List all events from a Google Calendar")
    @IArgument(name = "name", description = "The name to use for the calendar", required = false)
    public static void list(SlashCommandInteractionEvent event) {
        String name = event.getOption("name") != null ? event.getOption("name").getAsString() : null;

        if(name == null) {
            List<String> calendarNames = calendarManager.getCalendarNames(event.getGuild().getIdLong());
            event.reply("Available calendars: " + String.join(", ", calendarNames)).queue();
        }
        else {
            event.reply("Not implemented yet").queue();
        }
    }

    @ICommand(name = "update", description = "Update configuration of a calendar", permissions = {Permission.ADMINISTRATOR})
    @IArgument(name = "name", description = "The name to use for the calendar", required = true)
    @IArgument(name = "channel", description = "The channel where events will be announced", required = true, type = OptionType.CHANNEL)
    @IArgument(name = "role", description = "The role to be pinged when events begin", required = false, type = OptionType.ROLE)
    public static void update(SlashCommandInteractionEvent event) {
        event.reply("Not implemented yet").queue();
    }

    // TODO: 30/10/2023 autocomplete of names when unsubscribing and updating
}
