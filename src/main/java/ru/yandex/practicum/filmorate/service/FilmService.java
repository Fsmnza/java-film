package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.FatalFilmException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public FilmDto createFilm(NewFilmRequest request) {
        if (request.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new FatalFilmException("Дата релиза должна быть не раньше 28.12.1895");
        }
        if (request.getDuration() <= 0) {
            throw new FatalFilmException("Длительность должна быть положительной");
        }
        Film film = FilmMapper.mapToFilm(request);
        return FilmMapper.mapToFilmDto(filmStorage.addFilm(film));
    }

    public FilmDto updateFilm(UpdateFilmRequest request) {
        Film film = filmStorage.getFilmById(request.getId());
        if (film == null) throw new NotFoundException("Фильм с id=" + request.getId() + " не найден");
        FilmMapper.updateFilmFields(film, request);
        return FilmMapper.mapToFilmDto(filmStorage.updateFilm(film));
    }

    public List<FilmDto> findAllFilms() {
        return filmStorage.getAllFilms().stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public void addLike(int userId, int filmId) {
        if (userStorage.getUserById(userId) == null) throw new NotFoundException("Пользователь не найден");
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) throw new NotFoundException("Фильм не найден");
        film.getLikes().add((long) userId);
    }

    public void removeLike(int userId, int filmId) {
        if (userStorage.getUserById(userId) == null) throw new NotFoundException("Пользователь не найден");
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) throw new NotFoundException("Фильм не найден");
        film.getLikes().remove((long) userId);
    }

    public List<FilmDto> getPopularFilmsByLikes(int limit) {
        return filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(limit)
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }
}
