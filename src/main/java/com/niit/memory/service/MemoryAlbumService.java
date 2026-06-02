package com.niit.memory.service;

import com.niit.memory.entity.MemoryAlbum;
import com.niit.memory.repository.MemoryAlbumRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemoryAlbumService {

    private final MemoryAlbumRepository repository;

    public MemoryAlbumService(MemoryAlbumRepository repository) {
        this.repository = repository;
    }

    public List<MemoryAlbum> findAll() {
        return repository.findAll();
    }

    public MemoryAlbum create(MemoryAlbum album) {
        repository.insert(album);
        return album;
    }

    public MemoryAlbum findById(Long id) {
        return repository.findById(id);
    }

    public MemoryAlbum update(MemoryAlbum album) {
        repository.update(album);
        return repository.findById(album.getId());
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
