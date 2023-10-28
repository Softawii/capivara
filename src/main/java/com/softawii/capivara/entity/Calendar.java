package com.softawii.capivara.entity;

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;

@Entity
public class Calendar {

    @EmbeddedId
    private CalendarKey calendarKey;

    @Embeddable
    public static class CalendarKey implements Serializable {

        @Column
        private Long guildId;

        @Column
        private String calendarName;

        public CalendarKey() {
        }

        public CalendarKey(Long guildId, String calendarName) {
            this.guildId = guildId;
            this.calendarName = calendarName;
        }

        public Long getGuildId() {
            return guildId;
        }

        public void setGuildId(Long guildId) {
            this.guildId = guildId;
        }

        public String getCalendarName() {
            return calendarName;
        }

        public void setCalendarName(String calendarName) {
            this.calendarName = calendarName;
        }

    }

    public Calendar(Long guildId, String googleCalendarId, String name, Long channelId, Long roleId) {
        this.calendarKey = new CalendarKey(guildId, name);
        this.googleCalendarId = googleCalendarId;
        this.channelId = channelId;
        this.roleId = roleId;
    }

    @Column
    private String googleCalendarId;

    @Column
    private Long channelId;

    @Column
    private Long roleId;

    public Calendar() {
    }

    public CalendarKey getCalendarKey() {
        return this.calendarKey;
    }

    public void setCalendarKey(CalendarKey calendarKey) {
        this.calendarKey = calendarKey;
    }

    public String getGoogleCalendarId() {
        return googleCalendarId;
    }

    public void setGoogleCalendarId(String googleCalendarId) {
        this.googleCalendarId = googleCalendarId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
