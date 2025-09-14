package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void addLike(int userId, int filmId) {
        User user = userStorage.getUserById(userId);
        Film film = filmStorage.getFilmById(filmId);
        if (user == null) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        if (film == null) throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        film.getLikes().add((long) userId);
    }

    public void removeLIke(int userId, int filmId) {
        User user = userStorage.getUserById(userId);
        Film film = filmStorage.getFilmById(filmId);

        if (user == null) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        if (film == null) throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        film.getLikes().remove((long) userId);
    }

    public List<Film> findAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film createFilm(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new RuntimeException("Дата релиза должна быть не раньше 28.12.1895");
        }
        if (film.getDuration() < 0) {
            throw new IllegalArgumentException("Длительность должна быть положительной");
        }
        return filmStorage.addFilm(film);
    }



    public Film updateFilm(Film film) {
        if (filmStorage.getFilmById(film.getId()) == null) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        return filmStorage.updateFilm(film);
    }


    public List<Film> getPopularFilmsByLikes(int number) {
        return filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(number)
                .toList();
    }
}
