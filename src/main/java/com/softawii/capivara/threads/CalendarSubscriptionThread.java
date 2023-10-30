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
import java.util.concurrent.*;

@Component
public class CalendarSubscriptionThread implements Runnable {

    /**
     * The queue of events to be processed, in order.
     * No need to synchronize, as it is a thread-safe queue.
     * @see java.util.concurrent.BlockingQueue
     *
     * Enqueue is made by any thread, dequeue is made by the thread that created the queue.
     * @see java.util.concurrent.BlockingQueue#put
     * @see java.util.concurrent.BlockingQueue#take
     *
     * If there is no event to be processed, the thread will wait until there is one.
     */
    private final Logger LOGGER = LogManager.getLogger(CalendarSubscriptionThread.class);
    private final BlockingQueue<String> queue;
    private final ScheduledExecutorService scheduler;
    private final JDA                      jda;
    private final GoogleCalendarService    googleCalendarService;
    private final CalendarService          calendarService;
    private final Map<String, CalendarSubscriber> subscribers;

    public CalendarSubscriptionThread(JDA jda, CalendarService calendarService, GoogleCalendarService googleCalendarService) {
        this.jda = jda;
        this.googleCalendarService = googleCalendarService;
        this.queue = new LinkedBlockingQueue<>();
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

        Thread thread = new Thread(this, "CalendarSubscriptionThread");
        thread.start();
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

    public void subscribe(Calendar calendar) {
        CalendarSubscriber subscriber = this.subscribers.get(calendar.getGoogleCalendarId());
        if (subscriber == null) {
            subscriber = new CalendarSubscriber(calendar.getGoogleCalendarId(), this.googleCalendarService, jda);
            this.subscribers.put(calendar.getGoogleCalendarId(), subscriber);
        }

        subscriber.subscribe(calendar);
    }

    public void unsubscribe(Calendar calendar) {
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

    @Override
    public void run() {
        while (true) {
            try {
                String event = queue.take();
                // Process the event
            } catch (InterruptedException e) {
                // The thread was interrupted, just exit
                return;
            }
        }
    }
}
