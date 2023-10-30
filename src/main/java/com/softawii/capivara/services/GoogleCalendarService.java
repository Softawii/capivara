package com.softawii.capivara.services;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.softawii.capivara.utils.CalendarUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleCalendarService {

    private final Logger LOGGER = LogManager.getLogger(GoogleCalendarService.class);

    private final Calendar calendar;

    public GoogleCalendarService(Calendar calendar) {
        this.calendar = calendar;
    }

    public List<Event> getEvents(String googleCalendarId, boolean removeStarted, boolean removeEnded) {
        LOGGER.info("Fetching events from calendar '{}' - removeStarted: {} - removeEnded: {}", googleCalendarId, removeStarted, removeEnded);
        Instant     now       = Instant.now();
        List<Event> items     = new ArrayList<>();
        String      pageToken = null;
        do {
            try {
                Events rawEvents = calendar
                        .events()
                        .list(googleCalendarId)
                        .setSingleEvents(true)
                        .setTimeMin(CalendarUtil.getMinDateTime(-1))
                        .setTimeMax(CalendarUtil.getMaxDateTime(1))
                        .setOrderBy("startTime")
                        .setPageToken(pageToken)
                        .execute();

                List<Event> events = rawEvents.getItems()
                        .stream()
                        .filter(event -> {
                            if (removeStarted && this.hasEventStarted(now, event)) {
                                return false;
                            }
                            if (removeEnded && this.hasEventEnded(now, event)) {
                                return false;
                            }

                            return true;
                        })
                        .toList();

                items.addAll(events);
                pageToken = rawEvents.getNextPageToken();
            } catch (GoogleJsonResponseException e) {
                if ((e.getStatusCode() == 403 && !e.getStatusMessage().equals("Forbidden")) || e.getStatusCode() == 429) {
                    LOGGER.error(e.getDetails().getMessage() + " - rate limit - " + e.getDetails().getCode());
                } else {
                    LOGGER.error(e.getDetails().getMessage());
                }
                return List.of();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                return List.of();
            }
        } while (pageToken != null);

        return items;
    }

    private boolean hasEventStarted(Instant now, Event event) {
        EventDateTime eventStart = event.getStart();
        DateTime      dateStart;

        boolean isAllDayEvent = eventStart.getDate() != null;
        if (isAllDayEvent) {
            dateStart = eventStart.getDate();
        } else {
            dateStart = eventStart.getDateTime();
        }

        return dateStart.getValue() <= now.toEpochMilli();
    }

    private boolean hasEventEnded(Instant now, Event event) {
        EventDateTime eventEnd = event.getEnd();
        DateTime      dateEnd;

        boolean isAllDayEvent = eventEnd.getDate() != null;
        if (isAllDayEvent) {
            dateEnd = eventEnd.getDate();
        } else {
            dateEnd = eventEnd.getDateTime();
        }

        return dateEnd.getValue() <= now.toEpochMilli();
    }
}
