package ru.yandex.practicum.filmorate.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.*;
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
        dto.setGenres(
                film.getGenres().stream()
                        .map(FilmMapper::mapToGenreDto)
                        .sorted(Comparator.comparingInt(GenreDto::getId))
                        .collect(Collectors.toList())
        );

        dto.setDirectors(
                film.getDirectors() == null ? Collections.emptyList() :
                        film.getDirectors().stream()
                                .map(d -> new DirectorDto(d.getId(), d.getName()))
                                .collect(Collectors.toList())
        );
        dto.setLikes(film.getLikes());

        return dto;
    }

    public static Film mapToFilm(NewFilmRequest request,
                                 Rating mpaRating,
                                 Set<Genre> genres,
                                 Set<Director> directors) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());
        film.setMpa(mpaRating);
        film.setGenres(new ArrayList<>(genres));
        film.setDirectors(directors != null ? new ArrayList<>(directors) : new ArrayList<>());
        return film;
    }


    public static Film updateFilmFields(Film film, UpdateFilmRequest request) {
        if (request.hasName()) {
            film.setName(request.getName());
        }
        if (request.hasDescription()) {
            film.setDescription(request.getDescription());
        }
        if (request.hasReleaseDate()) {
            film.setReleaseDate(request.getReleaseDate());
        }
        if (request.hasDuration()) {
            film.setDuration(request.getDuration());
        }
        return film;
    }

    private static GenreDto mapToGenreDto(Genre genre) {
        GenreDto dto = new GenreDto();
        dto.setId(genre.getId());
        dto.setName(genre.getName());
        return dto;
    }
}
