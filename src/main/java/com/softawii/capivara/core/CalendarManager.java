package com.softawii.capivara.core;

import com.softawii.capivara.entity.Calendar;
import com.softawii.capivara.exceptions.DuplicatedKeyException;
import com.softawii.capivara.services.CalendarService;
import com.softawii.capivara.threads.CalendarSubscriptionManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class CalendarManager {

    private final Logger LOGGER = LogManager.getLogger(CalendarManager.class);
    private final JDA jda;
    private final CalendarService service;
    private final CalendarSubscriptionManager subscriber;

    public CalendarManager(JDA jda, CalendarService service, CalendarSubscriptionManager subscriber) {
        this.jda = jda;
        this.service = service;
        this.subscriber = subscriber;
    }

    public void createCalendar(String googleCalendarId, String name, GuildChannelUnion channel, Role role) throws DuplicatedKeyException {
        Long guildId = channel.getGuild().getIdLong();
        Long channelId = channel.getIdLong();
        Long roleId = role != null ? role.getIdLong() : null;
        Calendar calendar = new Calendar(guildId, googleCalendarId, name, channelId, roleId);

        this.service.create(calendar);
        this.subscriber.subscribe(calendar);
    }
}
