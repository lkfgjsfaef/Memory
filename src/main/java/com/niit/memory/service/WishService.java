package com.niit.memory.service;

import com.niit.memory.config.UserContext;
import com.niit.memory.entity.Wish;
import com.niit.memory.repository.WishRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WishService {

    private final WishRepository repository;

    public WishService(WishRepository repository) {
        this.repository = repository;
    }

    public List<Wish> findAll(String status, String category) {
        return repository.findAll(status, category);
    }

    public Wish create(Wish wish) {
        wish.setUserId(UserContext.getUserId());
        repository.insert(wish);
        return wish;
    }

    public Wish update(Wish wish) {
        repository.update(wish);
        return repository.findById(wish.getId());
    }

    public void updateStatus(Long id, String status) {
        repository.updateStatus(id, status);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", repository.countAll());
        stats.put("pending", repository.countByStatus("pending"));
        stats.put("in_progress", repository.countByStatus("in_progress"));
        stats.put("completed", repository.countByStatus("completed"));
        return stats;
    }
}
