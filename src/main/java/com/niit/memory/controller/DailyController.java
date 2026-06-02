package com.niit.memory.controller;

import com.niit.memory.config.Result;
import com.niit.memory.entity.DailyRecord;
import com.niit.memory.service.DailyRecordService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/daily-records")
public class DailyController {

    private final DailyRecordService service;

    public DailyController(DailyRecordService service) {
        this.service = service;
    }

    @GetMapping
    public Result list(@RequestParam(required = false) Integer year,
                       @RequestParam(required = false) Integer month) {
        try {
            return Result.success(service.findAll(year, month));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/stats")
    public Result stats() {
        try {
            return Result.success(service.getStats());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result create(@RequestBody DailyRecord record) {
        try {
            service.create(record);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody DailyRecord record) {
        try {
            record.setId(id);
            return Result.success(service.update(record));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
