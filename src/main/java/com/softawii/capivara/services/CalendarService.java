package com.softawii.capivara.services;

import com.softawii.capivara.entity.Calendar;
import com.softawii.capivara.repository.CalendarRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CalendarService {

    private final CalendarRepository repository;

    public CalendarService(CalendarRepository repository) {
        this.repository = repository;
    }

    public Page<Calendar> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<Calendar> find(Calendar.CalendarKey calendarKey) {
        return repository.findById(calendarKey);
    }

    public void create(Calendar calendar) {
        if (repository.existsById(calendar.getCalendarKey())) {
            // TODO: create new exception
            throw new RuntimeException("Calendar already exists with this name and guild");
        }
        repository.save(calendar);
    }

    public void update(Calendar calendar) {
        if (!repository.existsById(calendar.getCalendarKey())) {
            // TODO: create new exception
            throw new RuntimeException("Calendar does not exist with this name and guild");
        }
        repository.save(calendar);
    }

    public void delete(Calendar.CalendarKey calendarKey) {
        repository.deleteById(calendarKey);
    }

    public void exists(Calendar.CalendarKey calendarKey) {
        repository.existsById(calendarKey);
    }
}
