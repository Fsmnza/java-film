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

    public List<FilmDto> getAll() {
        return filmRepository.getAll().stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public FilmDto getById(int id) {
        Film film = filmRepository.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto create(NewFilmRequest request) {
        Rating mpaRating = ratingRepository.getById(request.getMpa().getId())
                .orElseThrow(() -> new NotFoundException("Рейтинг с id = " + request.getMpa().getId() + " не найден"));

        Set<Genre> genres = new HashSet<>();
        if (request.getGenres() != null) {
            for (GenreDto genreDto : request.getGenres()) {
                genres.add(genreRepository.getById(genreDto.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с id = " + genreDto.getId() + " не найден")));
            }
        }
        Set<Director> directors = new HashSet<>();
        if (request.getDirectors() != null) {
            Set<Integer> directorIds = request.getDirectors().stream()
                    .map(Director::getId)
                    .collect(Collectors.toSet());

            directors = directorRepository.getAllDirectors().stream()
                    .filter(d -> directorIds.contains(d.getId()))
                    .collect(Collectors.toSet());
        }

        Film film = FilmMapper.mapToFilm(request, mpaRating, genres, directors);
        filmRepository.create(film);
        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto update(UpdateFilmRequest request) {
        Film film = filmRepository.getById(request.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + request.getId() + " не найден"));

        Film updatedFilm = FilmMapper.updateFilmFields(film, request);
        updatedFilm = filmRepository.update(updatedFilm);
        return FilmMapper.mapToFilmDto(updatedFilm);
    }

    public void putLike(int filmId, int userId) {
        if (filmRepository.getById(filmId).isEmpty())
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        if (userRepository.getById(userId).isEmpty())
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");

        if (filmRepository.getLikesUserId(filmId).contains(userId))
            throw new ValidationException("Пользователь с id = " + userId + " уже поставил лайк фильму с id = " + filmId);
        if (filmRepository.getById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        filmRepository.putLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        if (filmRepository.getById(filmId).isEmpty())
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        if (userRepository.getById(userId).isEmpty())
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");

        filmRepository.removeLike(filmId, userId);
    }

    public List<FilmDto> getPopular(int count) {
        if (count <= 0)
            throw new ValidationException("Количество фильмов должно быть положительным числом");

        return filmRepository.getPopular(count).stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getFilmsByDirectorSortedByLikes(Integer directorId) {
        List<Film> films = filmRepository.getFilmsByDirectorSortedByLikes(directorId);
        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getFilmsByDirectorSortedByYear(Integer directorId) {
        List<Film> films = filmRepository.getFilmsByDirectorSortedByYear(directorId);
        if (films.isEmpty())
            throw new NotFoundException("Фильмы режиссера с id = " + directorId + " не найдены");

        return films.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }
}
