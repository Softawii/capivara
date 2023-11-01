package com.softawii.capivara.repository;

import com.softawii.capivara.entity.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarRepository extends JpaRepository<Calendar, Calendar.CalendarKey> {
    Calendar findByCalendarKey(Calendar.CalendarKey calendarKey);
    List<Calendar> findAllByCalendarKeyGuildId(Long guildId);
}
