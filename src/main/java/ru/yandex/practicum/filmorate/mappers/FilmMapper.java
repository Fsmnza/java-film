package ru.yandex.practicum.filmorate.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilmMapper {
    public static FilmDto mapToFilmDto(Film film) {
        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());
        dto.setMpa(film.getMpa());
        dto.setLikes(film.getLikes() != null ? film.getLikes() : new HashSet<>());
        if (film.getGenres() != null) {
            dto.setGenres(film.getGenres());
        }
        if (film.getDirectors() != null) {
            dto.setDirectors(
                    film.getDirectors().stream()
                            .sorted(Comparator.comparingInt(Director::getId))
                            .map(DirectorMapper::toDto)
                            .collect(Collectors.toCollection(LinkedHashSet::new))
            );
        }

        return dto;
    }

    public static Film mapToFilm(NewFilmRequest request, Rating mpaRating, Set<Genre> genres, Set<Director> directors) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());
        film.setMpa(mpaRating);
        film.setGenres(genres);
        film.setDirectors(directors);
        return film;
    }

    public static Film updateFilmFields(Film film, UpdateFilmRequest request) {
        if (request.hasName()) film.setName(request.getName());
        if (request.hasDescription()) film.setDescription(request.getDescription());
        if (request.hasReleaseDate()) film.setReleaseDate(request.getReleaseDate());
        if (request.hasDuration()) film.setDuration(request.getDuration());
        return film;
    }
}
