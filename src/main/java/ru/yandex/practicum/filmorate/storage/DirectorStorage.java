package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    List<Director> findAll();

    Optional<Director> findById(int id);

    Director create(Director director);

    Director update(Director director);

    void delete(int id);

    List<Film> getFilmsByDirectorSortedByYear(int directorId);

    List<Film> getFilmsByDirectorSortedByLikes(int directorId);
}
