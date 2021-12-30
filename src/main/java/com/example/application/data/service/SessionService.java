package com.example.application.data.service;

import com.example.application.data.entity.Session;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private SessionRepository repository;

    public SessionService(@Autowired SessionRepository repository) {
        this.repository = repository;
    }

    public Optional<Session> get(Integer id) {
        return repository.findById(id);
    }

    public Session update(Session entity) {
        return repository.save(entity);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public Page<Session> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
