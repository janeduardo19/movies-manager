package com.example.application.data.service;

import com.example.application.data.entity.Movie;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MovieService {

    private MovieRepository repository;

    public MovieService(@Autowired MovieRepository repository) {
        this.repository = repository;
    }

    public Optional<Movie> get(Integer id) {
        return repository.findById(id);
    }

    public Movie update(Movie entity) {
        return repository.save(entity);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public Page<Movie> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
