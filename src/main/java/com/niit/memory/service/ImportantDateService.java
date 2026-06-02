package com.niit.memory.service;

import com.niit.memory.entity.ImportantDate;
import com.niit.memory.repository.ImportantDateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImportantDateService {

    private final ImportantDateRepository repository;

    public ImportantDateService(ImportantDateRepository repository) {
        this.repository = repository;
    }

    public List<Map<String, Object>> findAllWithDaysLeft() {
        List<ImportantDate> dates = repository.findAll();
        List<Map<String, Object>> upcoming = new ArrayList<>();
        List<Map<String, Object>> passedRecurring = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (ImportantDate d : dates) {
            if (d.getEventDate() == null) continue;
            long daysLeft = ChronoUnit.DAYS.between(today, d.getEventDate());
            boolean isRecurring = d.getRecurring() != null && d.getRecurring() == 1;

            // Skip non-recurring dates that have passed
            if (daysLeft < 0 && !isRecurring) continue;

            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("title", d.getTitle());
            map.put("icon", d.getIcon());
            map.put("date", d.getEventDate().toString());
            map.put("eventDate", d.getEventDate().toString());
            map.put("lunarDate", d.getLunarDate() != null ? d.getLunarDate() : "");
            map.put("note", d.getNote() != null ? d.getNote() : "");
            map.put("recurring", d.getRecurring());
            map.put("recurringMonth", d.getRecurringMonth());
            map.put("recurringDay", d.getRecurringDay());
            map.put("daysLeft", daysLeft);

            if (daysLeft >= 0) {
                upcoming.add(map);
            } else {
                passedRecurring.add(map);
            }
        }

        // Sort upcoming by daysLeft ascending (soonest first)
        upcoming.sort((a, b) -> Long.compare((long) a.get("daysLeft"), (long) b.get("daysLeft")));
        // Sort passed recurring by daysLeft descending (most recently passed first)
        passedRecurring.sort((a, b) -> Long.compare((long) b.get("daysLeft"), (long) a.get("daysLeft")));

        List<Map<String, Object>> result = new ArrayList<>();
        result.addAll(upcoming);
        result.addAll(passedRecurring);
        return result;
    }

    public ImportantDate create(ImportantDate d) {
        if (d.getEventDate() == null) {
            throw new IllegalArgumentException("eventDate must not be null");
        }
        repository.insert(d);
        return d;
    }

    public ImportantDate update(ImportantDate d) {
        repository.update(d);
        return repository.findById(d.getId());
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
