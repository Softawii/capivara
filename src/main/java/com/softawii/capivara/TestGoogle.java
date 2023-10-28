package com.softawii.capivara;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import com.softawii.capivara.utils.CalendarUtil;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.List;

public class TestGoogle {
    public static void main(String[] args) throws GeneralSecurityException, IOException {
        String           calendarId    = "";
        String           googleApiKey  = System.getenv("googleApiKey");
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory      factory       = GsonFactory.getDefaultInstance();

        Calendar service = new Calendar.Builder(httpTransport, factory, null)
                .setApplicationName("Capivara")
                .setGoogleClientRequestInitializer(request -> request.set("key", googleApiKey))
                .build();

        DateTime minDateTime = CalendarUtil.getMinDateTime(0);
        DateTime maxDateTime = CalendarUtil.getMaxDateTime(0);
        Instant  now         = Instant.now();
        System.out.printf("minDateTime: %s :: maxDateTime: %s%n", minDateTime, maxDateTime);
        String pageToken = null;
        System.out.println("Events ---------------------");
        do {
            try {
                Events events = service
                        .events()
                        .list(calendarId)
                        .setSingleEvents(true)
                        .setTimeMin(minDateTime)
                        .setTimeMax(maxDateTime)
                        .setOrderBy("startTime")
                        .setPageToken(pageToken)
                        .execute();
                List<Event> items = events.getItems();
                for (Event event : items) {
                    String title       = event.getSummary();
                    String description = event.getDescription();

                    EventDateTime eventStart    = event.getStart();
                    EventDateTime eventEnd      = event.getEnd();
                    DateTime      dateStart;
                    DateTime      dateEnd;
                    boolean       isAllDayEvent = eventStart.getDate() != null && eventEnd.getDate() != null;
                    if (isAllDayEvent) {
                        dateStart = eventStart.getDate();
                        dateEnd = eventEnd.getDate();
                    } else {
                        dateStart = eventStart.getDateTime();
                        dateEnd = eventEnd.getDateTime();
                    }
                    boolean alreadyStarted = dateStart.getValue() <= now.toEpochMilli();
                    boolean alreadyEnded   = dateEnd.getValue() <= now.toEpochMilli();
                    System.out.printf("""
                                              ID: %s
                                              Title: %s
                                              Description: %s
                                              Start: %s
                                              End: %s
                                              All day event: %s
                                              Already started: %s
                                              Already ended: %s
                                              -------------------------
                                              """,
                                      event.getId(),
                                      title,
                                      description,
                                      dateStart,
                                      dateEnd,
                                      isAllDayEvent ? "yes" : "no",
                                      alreadyStarted ? "yes" : "no",
                                      alreadyEnded ? "yes" : "no"
                    );
                }
                pageToken = events.getNextPageToken();
            } catch (GoogleJsonResponseException e) {
                System.out.println(e.getDetails());
                if ((e.getStatusCode() == 403 && !e.getStatusMessage().equals("Forbidden")) || e.getStatusCode() == 429) {
                    System.out.println("rate limited");
                } else {
                    e.printStackTrace();
                }
            }
        } while (pageToken != null);
    }

}
