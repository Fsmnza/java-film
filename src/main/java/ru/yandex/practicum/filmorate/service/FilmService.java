package ru.yandex.practicum.filmorate.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmRepository filmRepository;
    private final RatingRepository ratingRepository;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;
    private final DirectorRepository directorRepository;
    private final FeedRepository feedRepository;

    @Autowired
    public FilmService(FilmRepository filmRepository,
                       GenreRepository genreRepository,
                       RatingRepository ratingRepository,
                       UserRepository userRepository,
                       DirectorRepository directorRepository, FeedRepository feedRepository) {
        this.filmRepository = filmRepository;
        this.genreRepository = genreRepository;
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.directorRepository = directorRepository;
        this.feedRepository = feedRepository;
    }

    @Transactional
    public FilmDto create(NewFilmRequest request) {

        Optional<Rating> maybeRating = ratingRepository.getById(request.getMpa().getId());
        if (maybeRating.isEmpty()) {
            throw new NotFoundException("Рейтинг с id = " + request.getMpa().getId() + " не найден");
        }
        Rating rating = maybeRating.get();
        Set<Genre> genres = new HashSet<>();
        if (request.getGenres() != null) {
            for (GenreDto genreDto : request.getGenres()) {
                Optional<Genre> maybeGenre = genreRepository.getById(genreDto.getId());
                if (maybeGenre.isEmpty()) {
                    throw new NotFoundException("Жанр с id = " + genreDto.getId() + " не найден");
                }
                genres.add(maybeGenre.get());
            }
        }
        Set<Director> directors = new HashSet<>();
        if (request.getDirectors() != null) {
            for (DirectorDto directorDto : request.getDirectors()) {
                Optional<Director> maybeDirector = directorRepository.findById(directorDto.getId());
                if (maybeDirector.isEmpty()) {
                    throw new NotFoundException("Режиссёр с id = " + directorDto.getId() + " не найден");
                }
                directors.add(maybeDirector.get());
            }
        }
        Film film = FilmMapper.mapToFilm(request, rating, genres, directors);
        filmRepository.create(film);
        return FilmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> getAll() {
        return filmRepository.getAll().stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto getById(int id) {
        Optional<Film> mainFilm = filmRepository.getById(id);
        if (mainFilm.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        return FilmMapper.mapToFilmDto(mainFilm.get());
    }

    @Transactional
    public FilmDto update(UpdateFilmRequest request) {
        Optional<Film> mainFilm = filmRepository.getById(request.getId());
        if (mainFilm.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + request.getId() + " не найден");
        }
        List<Director> directors = null;
        if (request.hasDirectors()) {
            directors = new ArrayList<>();
            for (DirectorDto directorDto : request.getDirectors()) {
                Optional<Director> maybeDirector = directorRepository.findById(directorDto.getId());
                if (maybeDirector.isEmpty()) {
                    throw new NotFoundException("Режиссёр с id = " + directorDto.getId() + " не найден");
                }
                directors.add(maybeDirector.get());
            }
        }
        Film updatedFilm = FilmMapper.updateFilmFields(mainFilm.get(), request, directors);
        updatedFilm = filmRepository.update(updatedFilm);
        return FilmMapper.mapToFilmDto(updatedFilm);
    }

    @Transactional
    public void putLike(int filmId, int userId) {
        if (filmRepository.getById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        if (userRepository.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (!filmRepository.getLikesUserId(filmId).contains(userId)) {
            filmRepository.putLike(filmId, userId);
        }
        feedRepository.create(new Feed(userId, filmId, EventType.LIKE, Operation.ADD));
    }

    @Transactional
    public void removeLike(int filmId, int userId) {
        if (filmRepository.getById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        if (userRepository.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        filmRepository.removeLike(filmId, userId);
        feedRepository.create(new Feed(userId, filmId, EventType.LIKE, Operation.REMOVE));
    }

    public List<FilmDto> getPopular(int count, Integer genreId, Integer year) {
        if (count <= 0) {
            throw new ValidationException("Количество фильмов должно быть положительным числом");
        }
        List<Film> popular = filmRepository.getPopular(count, genreId, year);
        return popular.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getFilmsByDirector(int directorId, String sortBy) {
        if (directorRepository.findById(directorId).isEmpty()) {
            throw new NotFoundException("Режиссёр с id = " + directorId + " не найден");
        }
        List<Film> films;
        if ("likes".equalsIgnoreCase(sortBy)) {
            films = filmRepository.getFilmsByDirectorSortedByLikes(directorId);
        } else if ("year".equalsIgnoreCase(sortBy)) {
            films = filmRepository.getFilmsByDirectorSortedByYear(directorId);
        } else {
            throw new IllegalArgumentException("Некорректный параметр сортировки: " + sortBy);
        }
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> searchFilms(String query, String by) {
        if (query == null || query.trim().isEmpty()) {
            throw new ValidationException("Поисковый запрос не может быть пустым");
        }

        String searchBy = (by != null) ? by.toLowerCase() : "title,director";
        List<Film> foundFilms;

        if (searchBy.contains("title") && searchBy.contains("director")) {
            foundFilms = filmRepository.searchFilmsByTitleAndDirector(query.trim());
        } else if (searchBy.contains("title")) {
            foundFilms = filmRepository.searchFilmsByTitle(query.trim());
        } else if (searchBy.contains("director")) {
            foundFilms = filmRepository.searchFilmsByDirector(query.trim());
        } else {
            throw new ValidationException("Неверный параметр by. Допустимые значения: title, director");
        }

        return foundFilms.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> searchCommonFilmsWithFriend(int userId, int friendId) {
        List<Film> foundFilms;
        userRepository.getById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id = " +
                userId + "не найден"));
        userRepository.getById(friendId).orElseThrow(() -> new NotFoundException("Друг с id = " +
                userId + "не найден"));

        foundFilms = filmRepository.getCommonFilmsWithFriend(userId, friendId);
        return foundFilms.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public void deleteById(int filmId) {
        if (filmRepository.getById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        filmRepository.deleteById(filmId);
    }
}