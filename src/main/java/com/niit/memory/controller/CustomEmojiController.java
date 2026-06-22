package com.niit.memory.controller;

import com.niit.memory.config.Result;
import com.niit.memory.entity.CustomEmoji;
import com.niit.memory.repository.CustomEmojiRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/custom-emojis")
public class CustomEmojiController {

    private final CustomEmojiRepository repository;

    public CustomEmojiController(CustomEmojiRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Result list() {
        try {
            return Result.success(repository.findAll());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result add(@RequestBody CustomEmoji emoji) {
        try {
            // Check duplicate
            if (repository.existsByEmojiUrl(emoji.getEmojiUrl())) {
                return Result.error("该表情已存在");
            }
            repository.insert(emoji);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            repository.deleteById(id);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
