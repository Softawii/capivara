package com.softawii.capivara.threads;

import com.softawii.capivara.entity.Calendar;
import com.softawii.capivara.entity.internal.CalendarSubscriber;
import com.softawii.capivara.services.CalendarService;
import com.softawii.capivara.services.GoogleCalendarService;
import net.dv8tion.jda.api.JDA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class CalendarSubscriptionManager {

    private final Logger LOGGER = LogManager.getLogger(CalendarSubscriptionManager.class);
    private final ScheduledExecutorService scheduler;
    private final JDA                      jda;
    private final GoogleCalendarService    googleCalendarService;
    private final CalendarService          calendarService;
    private final Map<String, CalendarSubscriber> subscribers;

    public CalendarSubscriptionManager(JDA jda, CalendarService calendarService, GoogleCalendarService googleCalendarService) {
        this.jda = jda;
        this.googleCalendarService = googleCalendarService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.calendarService = calendarService;
        this.subscribers = new HashMap<>();
        this.setup();
        LOGGER.info("CalendarSubscriptionThread started");
    }

    private void setup() {
        startupSubscriber();
        this.scheduler.scheduleAtFixedRate(() -> {
            LOGGER.info("Updating calendar events");
            this.subscribers.values().forEach(CalendarSubscriber::update);
        }, 0, 1, TimeUnit.MINUTES);
    }

    private void startupSubscriber() {
        PageRequest request = PageRequest.of(0, 100);
        Page<Calendar> calendarPage = this.calendarService.findAll(request);
        while (calendarPage.hasContent()) {
            calendarPage.forEach(this::subscribe);
            request = request.next();
            calendarPage = this.calendarService.findAll(request);
        }
    }

    public synchronized void subscribe(Calendar calendar) {
        CalendarSubscriber subscriber = this.subscribers.get(calendar.getGoogleCalendarId());
        if (subscriber == null) {
            subscriber = new CalendarSubscriber(calendar.getGoogleCalendarId(), this.googleCalendarService, jda);
            this.subscribers.put(calendar.getGoogleCalendarId(), subscriber);
        }

        subscriber.subscribe(calendar);
    }

    public synchronized void unsubscribe(Calendar calendar) {
        CalendarSubscriber subscriber = this.subscribers.get(calendar.getGoogleCalendarId());
        if (subscriber == null) {
            throw new RuntimeException("There is no subscription available");
        }

        subscriber.unsubscribe(calendar);
        if (!subscriber.isThereAnyConsumer()) {
            subscriber.purge();
            this.subscribers.remove(calendar.getGoogleCalendarId());
        }
    }
}
