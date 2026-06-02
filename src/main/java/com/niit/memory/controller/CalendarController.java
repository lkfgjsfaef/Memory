package com.niit.memory.controller;

import com.niit.memory.config.Result;
import com.niit.memory.entity.CalendarMood;
import com.niit.memory.entity.CalendarNote;
import com.niit.memory.service.CalendarMoodService;
import com.niit.memory.service.CalendarNoteService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarNoteService service;
    private final CalendarMoodService moodService;

    public CalendarController(CalendarNoteService service, CalendarMoodService moodService) {
        this.service = service;
        this.moodService = moodService;
    }

    @GetMapping("/notes")
    public Result list(@RequestParam int year, @RequestParam int month) {
        try {
            return Result.success(service.findByYearAndMonth(year, month));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/notes")
    public Result create(@RequestBody CalendarNote note) {
        try {
            service.create(note);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/notes/{id}")
    public Result update(@PathVariable Long id, @RequestBody CalendarNote note) {
        try {
            note.setId(id);
            return Result.success(service.update(note));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/notes/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/moods")
    public Result listMoods(@RequestParam int year, @RequestParam int month) {
        try {
            return Result.success(moodService.findByYearAndMonth(year, month));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/moods")
    public Result upsertMood(@RequestBody CalendarMood mood) {
        try {
            return Result.success(moodService.upsert(mood));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
