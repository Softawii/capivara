package com.softawii.capivara.repository;

import com.softawii.capivara.entity.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar, Calendar.CalendarKey> {
    Calendar findByCalendarKey(Calendar.CalendarKey calendarKey);
    List<Calendar> findAllByCalendarKeyGuildId(Long guildId);
}
