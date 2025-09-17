package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Service
public class GenreService {
    private final GenreRepository genreRepository;

    @Autowired
    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public List<Genre> getAll() {
        return genreRepository.getAll();
    }

    public Genre getById(int id) {
        Optional<Genre> maybeGenre = genreRepository.getById(id);
        if (maybeGenre.isEmpty()) {
            throw new NotFoundException("Жанр с id = " + id + " не найден");
        }
        return maybeGenre.get();
    }
}
