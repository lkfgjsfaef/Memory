package com.niit.memory.controller;

import com.niit.memory.config.Result;
import com.niit.memory.entity.Couple;
import com.niit.memory.entity.ImportantDate;
import com.niit.memory.service.CoupleService;
import com.niit.memory.service.ImportantDateService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HomeController {

    private final CoupleService coupleService;
    private final ImportantDateService importantDateService;

    public HomeController(CoupleService coupleService, ImportantDateService importantDateService) {
        this.coupleService = coupleService;
        this.importantDateService = importantDateService;
    }

    @GetMapping("/couple")
    public Result getCouple() {
        try {
            return Result.success(coupleService.getCoupleInfo());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/couple")
    public Result updateCouple(@RequestBody Couple couple) {
        try {
            return Result.success(coupleService.update(couple));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/important-dates")
    public Result getImportantDates() {
        try {
            return Result.success(importantDateService.findAllWithDaysLeft());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/important-dates")
    public Result createImportantDate(@RequestBody ImportantDate date) {
        try {
            importantDateService.create(date);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/important-dates/{id}")
    public Result updateImportantDate(@PathVariable Long id, @RequestBody ImportantDate date) {
        try {
            date.setId(id);
            return Result.success(importantDateService.update(date));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/important-dates/{id}")
    public Result deleteImportantDate(@PathVariable Long id) {
        try {
            importantDateService.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
