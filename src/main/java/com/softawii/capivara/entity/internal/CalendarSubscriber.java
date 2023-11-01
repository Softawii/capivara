package com.softawii.capivara.entity.internal;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.softawii.capivara.entity.Calendar;
import com.softawii.capivara.services.GoogleCalendarService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class CalendarSubscriber {
    private final Logger                        LOGGER = LogManager.getLogger(CalendarSubscriber.class);
    private final String                        googleCalendarId;
    private final List<Calendar>                consumers;
    private final GoogleCalendarService         googleCalendarService;
    private final HashMap<String, EventWrapper> events;
    private final JDA                           jda;

    private class EventWrapper extends TimerTask {
        private static final Logger LOGGER = LogManager.getLogger(EventWrapper.class);
        private final        Event  event;
        private final        Timer  timer;

        public EventWrapper(Event event) {
            this.event = event;
            this.timer = new Timer("EventWrapper-" + event.getId());
            schedule();
        }

        private void schedule() {
            EventDateTime eventStart    = this.event.getStart();
            EventDateTime eventEnd      = this.event.getEnd();
            boolean       isAllDayEvent = eventStart.getDate() != null && eventEnd.getDate() != null;
            DateTime      dateStart;

            if (isAllDayEvent) {
                dateStart = eventStart.getDate();
            } else {
                dateStart = eventStart.getDateTime();
            }

            Date scheduled = new Date(dateStart.getValue());
            this.timer.schedule(this, scheduled);
            LOGGER.info("Event scheduled: " + this.event.getSummary() + ", date: " + scheduled);
        }

        public void purge() {
            this.timer.cancel();
            this.timer.purge();
        }

        @Override
        public void run() {
            LOGGER.info("Event started: " + this.event.getSummary());
            EventDateTime eventStart    = this.event.getStart();
            boolean       isAllDayEvent = eventStart.getDate() != null;
            DateTime      dateStart;

            if (isAllDayEvent) {
                dateStart = eventStart.getDate();
            } else {
                dateStart = eventStart.getDateTime();
            }

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle(event.getSummary());
            embedBuilder.setDescription(event.getDescription());
            embedBuilder.addField("Hora", dateStart.toStringRfc3339(), false);

            MessageEmbed embed = embedBuilder.build();
            dispatchMessage(embed);
        }
    }

    public CalendarSubscriber(String googleCalendarId, GoogleCalendarService googleCalendarService, JDA jda) {
        this.googleCalendarId = googleCalendarId;
        this.googleCalendarService = googleCalendarService;
        this.jda = jda;
        this.events = new HashMap<>();
        this.consumers = new ArrayList<>();

        // Forcing Get Event Info
        this.update();
    }

    public void subscribe(Calendar consumer) {
        if (!this.consumers.contains(consumer)) {
            this.consumers.add(consumer);
        }
    }

    public void unsubscribe(Calendar consumer) {
        this.consumers.remove(consumer);
    }

    public boolean isThereAnyConsumer() {
        return !this.consumers.isEmpty();
    }

    public void purge() {
        this.events.values().forEach(EventWrapper::purge);
        this.events.clear();
    }

    private void checkForUpdates(List<Event> events) {
        // Removing events
        Set<String> localKeys = new HashSet<>(this.events.keySet());
        Set<String> remoteKeys = events.stream().map(Event::getId).collect(Collectors.toSet());
        localKeys.removeAll(remoteKeys);

        // Removing events that are not being listed
        for (String key : localKeys) {
            EventWrapper eventWrapper = this.events.get(key);
            LOGGER.info("Event removed: " + eventWrapper.event.getSummary());
            eventWrapper.purge();
            this.events.remove(key);
        }

        // Updating events
        for (Event event : events) {
            String eventId = event.getId();
            EventWrapper eventWrapper = this.events.get(eventId);
            if (eventWrapper == null) {
                this.events.put(eventId, new EventWrapper(event));
                LOGGER.info("Event added: " + event.getSummary());
            } else {
                eventWrapper.purge();
                this.events.put(eventId, new EventWrapper(event));
                LOGGER.info("Event updated: " + event.getSummary());
            }
        }
    }

    public synchronized void update() {
        LOGGER.info("Updating calendar events for calendar '{}'", this.googleCalendarId);
        List<Event> events = this.googleCalendarService.getEvents(this.googleCalendarId, true, true);
        if (!events.isEmpty()) {
            checkForUpdates(events);
        }
    }

    protected synchronized void dispatchMessage(MessageEmbed embed) {
        // TODO: bulk request
        // TODO: automatic delete wrapper after dispatch?
        //  - maybe a list of wrappers to remove and a faster schedule?
        this.consumers.forEach(calendar -> {
            TextChannel textChannel = this.jda.getTextChannelById(calendar.getChannelId());
            Role        role        = calendar.getRoleId() != null ? this.jda.getRoleById(calendar.getRoleId()) : null;
            if (textChannel != null) {
                MessageCreateAction messageAction = textChannel.sendMessageEmbeds(embed);
                if (role != null) {
                    messageAction = messageAction.mentionRoles(role.getId());
                }

                messageAction.submit();
            }
        });
    }
}
