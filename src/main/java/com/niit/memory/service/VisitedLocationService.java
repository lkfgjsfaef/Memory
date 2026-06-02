package com.niit.memory.service;

import com.niit.memory.entity.VisitedLocation;
import com.niit.memory.repository.VisitedLocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VisitedLocationService {

    private final VisitedLocationRepository repository;

    public VisitedLocationService(VisitedLocationRepository repository) {
        this.repository = repository;
    }

    public List<VisitedLocation> findAll() {
        return repository.findAll();
    }

    public VisitedLocation create(VisitedLocation location) {
        Long id = repository.insert(location);
        location.setId(id);
        return location;
    }

    public VisitedLocation update(VisitedLocation location) {
        repository.update(location);
        return location;
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public int count() {
        return repository.count();
    }
}
