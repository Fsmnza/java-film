package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmRepository filmRepository;
    private final RatingRepository ratingRepository;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;
    private final DirectorRepository directorRepository;

    @Autowired
    public FilmService(FilmRepository filmRepository,
                       GenreRepository genreRepository,
                       RatingRepository ratingRepository,
                       UserRepository userRepository,
                       DirectorRepository directorRepository) {
        this.filmRepository = filmRepository;
        this.genreRepository = genreRepository;
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.directorRepository = directorRepository;
    }

    public FilmDto create(NewFilmRequest request) {
        Rating mpaRating = ratingRepository.getById(request.getMpa().getId())
                .orElseThrow(() -> new NotFoundException("Рейтинг с id = " + request.getMpa().getId() + " не найден"));
        Set<Genre> genres = new HashSet<>();
        if (request.getGenres() != null) {
            for (GenreDto genreDto : request.getGenres()) {
                Genre genre = genreRepository.getById(genreDto.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с id = " + genreDto.getId() + " не найден"));
                genres.add(genre);
            }
        }

        Set<Director> directors = new HashSet<>();
        if (request.getDirectors() != null) {
            for (DirectorDto directorDto : request.getDirectors()) {
                Director found = directorRepository.findById(directorDto.getId())
                        .orElseThrow(() -> new NotFoundException("Режиссёр с id=" + directorDto.getId() + " не найден"));
                directors.add(found);
            }
        }


        Film film = FilmMapper.mapToFilm(request, mpaRating, genres, directors);

        if (film.getDirectors() == null) {
            film.setDirectors(new ArrayList<>());
        }

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

    public FilmDto update(UpdateFilmRequest request) {
        Film updatedFilm = filmRepository.getById(request.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + request.getId() + " не найден"));

        updatedFilm = FilmMapper.updateFilmFields(updatedFilm, request);

        if (request.getDirectors() != null) {
            Set<Director> directors = new HashSet<>();
            for (DirectorDto directorDto : request.getDirectors()) {
                Director found = directorRepository.findById(directorDto.getId())
                        .orElseThrow(() -> new NotFoundException("Режиссёр с id=" + directorDto.getId() + " не найден"));
                directors.add(found);
            }
            updatedFilm.setDirectors(new ArrayList<>(directors));
        }

        updatedFilm = filmRepository.update(updatedFilm);
        return FilmMapper.mapToFilmDto(updatedFilm);
    }


    public void putLike(int filmId, int userId) {
        if (filmRepository.getById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        if (userRepository.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (filmRepository.getLikesUserId(filmId).contains(userId)) {
            throw new ValidationException("Пользователь с id = " + userId +
                    " уже поставил лайк фильму с id = " + filmId);
        }
        filmRepository.putLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        if (filmRepository.getById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        if (userRepository.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        filmRepository.removeLike(filmId, userId);
    }

    public List<FilmDto> getPopular(int count) {
        if (count <= 0) {
            throw new ValidationException("Количество фильмов должно быть положительным числом");
        }
        List<Film> popular = filmRepository.getPopular(count);
        return popular.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getFilmsByDirector(int directorId, String sortBy) {
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
}