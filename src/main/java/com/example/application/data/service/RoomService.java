package com.example.application.data.service;

import com.example.application.data.entity.Room;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class RoomService {

    private RoomRepository repository;

    public RoomService(@Autowired RoomRepository repository) {
        this.repository = repository;
    }

    public Optional<Room> get(Integer id) {
        return repository.findById(id);
    }

    public Room update(Room entity) {
        return repository.save(entity);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public Page<Room> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
