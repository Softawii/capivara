package com.softawii.capivara.entity.internal;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.softawii.capivara.core.CalendarManager;
import com.softawii.capivara.entity.Calendar;
import com.softawii.capivara.utils.CalendarUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class CalendarSubscriber {
    private final Logger LOGGER = LogManager.getLogger(CalendarSubscriber.class);
    private String googleCalendarId;
    private List<Calendar.CalendarKey> consumers;
    private com.google.api.services.calendar.Calendar calendarService;
    private HashMap<String, EventWrapper> events;

    private class EventWrapper extends TimerTask {
        private Event event;
        private final Timer timer;

        public EventWrapper(Event event) {
            this.event = event;
            this.timer = new Timer();
            setTimer();
        }

        private void setTimer() {
            EventDateTime eventStart    = this.event.getStart();
            EventDateTime eventEnd      = this.event.getEnd();
            Instant now                 = Instant.now();
            DateTime dateStart;
            DateTime dateEnd;
            boolean isAllDayEvent = eventStart.getDate() != null && eventEnd.getDate() != null;

            if (isAllDayEvent) {
                dateStart = eventStart.getDate();
                dateEnd = eventEnd.getDate();
            } else {
                dateStart = eventStart.getDateTime();
                dateEnd = eventEnd.getDateTime();
            }

            boolean alreadyStarted = dateStart.getValue() <= now.toEpochMilli();
            boolean alreadyEnded   = dateEnd.getValue() <= now.toEpochMilli();

            if(!alreadyEnded && !alreadyStarted) {
                Date scheduled = new Date(dateStart.getValue());
                LOGGER.info("Event scheduled: " + this.event.getSummary() + ", date: " + scheduled);
                this.timer.schedule(this, scheduled);
            }
        }

        public void setEvent(Event event) {
            if(this.event.getStart() != event.getStart()) {
                this.timer.cancel();
                this.timer.purge();
                setTimer();
            }

            this.event = event;
        }

        @Override
        public void run() {
            LOGGER.info("Event started: " + this.event.getSummary());
        }
    }

    public CalendarSubscriber(String googleCalendarId, com.google.api.services.calendar.Calendar calendarService, Calendar.CalendarKey consumer) {
        this.googleCalendarId = googleCalendarId;
        this.calendarService = calendarService;
        this.events = new HashMap<>();
        this.consumers = new ArrayList<>();
        this.consumers.add(consumer);
        update();
    }

    public void subscribe(Calendar.CalendarKey consumer) {
        if (!this.consumers.contains(consumer)) {
            this.consumers.add(consumer);
        }
    }

    public void unsubscribe(Calendar.CalendarKey consumer) {
        this.consumers.remove(consumer);
    }

    private List<Event> getEvents() {
        List<Event> items = new ArrayList<>();
        String pageToken = null;
        do {
            try {
                Events events = calendarService
                        .events()
                        .list(googleCalendarId)
                        .setSingleEvents(true)
                        .setTimeMin(CalendarUtil.getMinDateTime(-1))
                        .setTimeMax(CalendarUtil.getMaxDateTime(1))
                        .setOrderBy("startTime")
                        .setPageToken(pageToken)
                        .execute();
                items.addAll(events.getItems());
                pageToken = events.getNextPageToken();
            } catch (GoogleJsonResponseException e) {
                if ((e.getStatusCode() == 403 && !e.getStatusMessage().equals("Forbidden")) || e.getStatusCode() == 429) {
                    LOGGER.error(e.getDetails().getMessage() + " - rate limit - " + e.getDetails().getCode());
                    return null;
                } else {
                    LOGGER.error(e.getDetails().getMessage());
                    return null;
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                return null;
            }
        } while (pageToken != null);

        return items;
    }

    private void checkForUpdates(List<Event> events) {
        // Removing events
        Set<String> localKeys = this.events.keySet();
        Set<String> remoteKeys = events.stream().map(Event::getId).collect(Collectors.toSet());
        localKeys.removeAll(remoteKeys);

        // Removing events
        for (String key : localKeys) {
            EventWrapper eventWrapper = this.events.get(key);
            LOGGER.info("Event removed: " + eventWrapper.event.getSummary());
            eventWrapper.cancel();
            eventWrapper.timer.purge();
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
                eventWrapper.setEvent(event);
            }
        }
    }

    public void update() {
        List<Event> events = getEvents();
        if (events != null) {
            checkForUpdates(events);
        }
    }
}
