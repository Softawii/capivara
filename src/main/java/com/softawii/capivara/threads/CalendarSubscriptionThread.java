package com.softawii.capivara.threads;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.softawii.capivara.entity.Calendar;
import com.softawii.capivara.services.CalendarService;
import net.dv8tion.jda.api.JDA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

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
    private final JDA jda;
    private final com.google.api.services.calendar.Calendar googleCalendarService;
    private final CalendarService calendarService;

    public CalendarSubscriptionThread(JDA jda, CalendarService calendarService, com.google.api.services.calendar.Calendar googleCalendarService) {
        this.jda = jda;
        this.googleCalendarService = googleCalendarService;
        this.queue = new LinkedBlockingQueue<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.calendarService = calendarService;
    }

    public void niceName() {
        PageRequest request = PageRequest.of(0, 100);
        Page<Calendar> calendarPage = this.calendarService.findAll(request);
        while (calendarPage.hasContent()) {
            calendarPage.forEach(calendar -> {
                Calendar.CalendarKey calendarKey = calendar.getCalendarKey();
                String name = calendarKey.getCalendarName();
                Long guildId = calendarKey.getGuildId();
            });

            request = request.next();
            calendarPage = this.calendarService.findAll(request);
        }
    }

    private void subscribe(Calendar calendar) throws IOException {
        String pageToken = null;
        do {
            Events events = googleCalendarService.events()
                    .list(calendar.getGoogleCalendarId())
                    .setPageToken(pageToken)
                    .execute();
            List<Event> items = events.getItems();
            for (Event event : items) {
                LOGGER.info(event.getSummary());
            }
            pageToken = events.getNextPageToken();
        } while (pageToken != null);
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
