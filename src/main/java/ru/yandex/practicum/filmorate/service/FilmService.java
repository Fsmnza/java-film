package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.RatingRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmRepository filmRepository;
    private final RatingRepository ratingRepository;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;

    @Autowired
    public FilmService(FilmRepository filmRepository, GenreRepository genreRepository,
                       RatingRepository ratingRepository, UserRepository userRepository) {
        this.filmRepository = filmRepository;
        this.genreRepository = genreRepository;
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
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

    public FilmDto create(NewFilmRequest request) {
        Optional<Rating> mainRaiting = ratingRepository.getById(request.getMpa().getId());
        if (mainRaiting.isEmpty()) {
            throw new NotFoundException("Рейтинг с id = " + request.getMpa().getId() + " не найден");
        }

        Rating mpaRating = mainRaiting.get();

        Set<Genre> genres = new HashSet<>();
        if (request.getGenres() != null) {
            for (GenreDto genreDto : request.getGenres()) {
                Optional<Genre> mainGemre = genreRepository.getById(genreDto.getId());
                if (mainGemre.isEmpty()) {
                    throw new NotFoundException("Жанр с id = " + genreDto.getId() + " не найден");
                }

                genres.add(mainGemre.get());
            }
        }

        Film film = FilmMapper.mapToFilm(request, mpaRating, genres);
        filmRepository.create(film);
        return FilmMapper.mapToFilmDto(film);
    }

    public FilmDto update(UpdateFilmRequest request) {
        Optional<Film> mainFilm = filmRepository.getById(request.getId());

        if (mainFilm.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + request.getId() + " не найден");
        }
        Film updatedFilm = FilmMapper.updateFilmFields(mainFilm.get(), request);
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
}