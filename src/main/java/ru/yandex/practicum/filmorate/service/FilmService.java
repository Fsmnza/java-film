package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.RatingDbStorage;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final FilmRepository filmRepository;
    private final RatingDbStorage ratingDbStorage;
    private final GenreDbStorage genreDbStorage;

    public FilmService(@Qualifier("dbStorage") FilmStorage filmStorage,
                       FilmRepository filmRepository,
                       RatingDbStorage ratingDbStorage,
                       GenreDbStorage genreDbStorage) {
        this.filmStorage = filmStorage;
        this.filmRepository = filmRepository;
        this.ratingDbStorage = ratingDbStorage;
        this.genreDbStorage = genreDbStorage;
    }

    public Film addFilm(Film film) {
        if (film.getRating() == null || film.getRating().getId() == null) {
            throw new ValidationException("Фильм должен иметь рейтинг MPA");
        }
        if (film.getRating() == null ||
                ratingDbStorage.getRatingById(film.getRating().getId()).isEmpty()) {
            throw new ValidationException("Некорректный рейтинг");
        }
        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> {
                if (genreDbStorage.getGenreById(genre.getId()).isEmpty()) {
                    throw new ValidationException("Некорректный жанр с id=" + genre.getId());
                }
            });
        }
        Film created = filmStorage.addFilm(film);
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreDbStorage.saveFilmGenres(created.getId(), film.getGenres());
        }
        return created;
    }

    public Film updateFilm(Film film) {
        if (film.getRating() == null ||
                ratingDbStorage.getRatingById(film.getRating().getId()).isEmpty()) {
            throw new ValidationException("Указан неверный MPA рейтинг");
        }
        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> {
                if (genreDbStorage.getGenreById(genre.getId()).isEmpty()) {
                    throw new ValidationException("Указан неверный жанр с id=" + genre.getId());
                }
            });
        }
        Film updated = filmStorage.updateFilm(film);
        genreDbStorage.deleteFilmGenres(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            genreDbStorage.saveFilmGenres(film.getId(), film.getGenres());
        }
        return updated;
    }

    public Optional<Film> findById(int id) {
        return Optional.ofNullable(filmStorage.getFilmById(id));
    }

    public Film getFilmById(int id) {
        Film film = filmStorage.getFilmById(id);
        if (film != null) {
            Set<Genre> genres = genreDbStorage.getGenresByFilmId(id)
                    .stream()
                    .collect(Collectors.toSet());
            film.setGenres(genres);
        }
        return film;
    }

    public List<Film> getAllFilms() {
        List<Film> films = filmStorage.getAllFilms();
        films.forEach(f -> {
            Set<Genre> genres = genreDbStorage.getGenresByFilmId(f.getId())
                    .stream()
                    .collect(Collectors.toSet());
            f.setGenres(genres);
        });
        return films;
    }

    public void deleteFilm(int id) {
        genreDbStorage.deleteFilmGenres(id);
        filmStorage.deleteFilm(id);
    }

    public void addLike(int filmId, int userId) {
        filmRepository.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        filmRepository.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        List<Film> films = filmRepository.getPopularFilms(count);
        films.forEach(f -> {
            Set<Genre> genres = genreDbStorage.getGenresByFilmId(f.getId())
                    .stream()
                    .collect(Collectors.toSet());
            f.setGenres(genres);
        });
        return films;
    }
}
