package com.niit.memory.controller;

import com.niit.memory.config.Result;
import com.niit.memory.entity.Wish;
import com.niit.memory.service.WishService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishes")
public class WishController {

    private final WishService service;

    public WishController(WishService service) {
        this.service = service;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WishController.class);

    @GetMapping
    public Result list(@RequestParam(required = false) String status,
                       @RequestParam(required = false) String category) {
        try {
            List<Wish> list = service.findAll(status, category);
            log.info("GET /api/wishes: status={} cat={} count={}", status, category, list.size());
            for (Wish w : list) {
                log.info("  wish id={} title={} imageUrls='{}'", w.getId(), w.getTitle(), w.getImageUrls());
            }
            return Result.success(list);
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
    public Result create(@RequestBody Wish wish) {
        try {
            log.info("POST /api/wishes: title={} imageUrls='{}' status={} category={}",
                wish.getTitle(), wish.getImageUrls(), wish.getStatus(), wish.getCategory());
            service.create(wish);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Wish wish) {
        try {
            log.info("PUT /api/wishes/{}: title={} imageUrls='{}' status={} category={}",
                id, wish.getTitle(), wish.getImageUrls(), wish.getStatus(), wish.getCategory());
            wish.setId(id);
            return Result.success(service.update(wish));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        try {
            String status = body.get("status");
            if (status == null || (!"pending".equals(status) && !"in_progress".equals(status) && !"completed".equals(status))) {
                return Result.error("Invalid status value: must be 'pending', 'in_progress', or 'completed'");
            }
            service.updateStatus(id, status);
            return Result.success();
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
