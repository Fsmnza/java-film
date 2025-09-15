//package ru.yandex.practicum.filmorate.mappers;
//
//import lombok.AccessLevel;
//import lombok.NoArgsConstructor;
//import ru.yandex.practicum.filmorate.dto.*;
//import ru.yandex.practicum.filmorate.model.Film;
//@NoArgsConstructor(access = AccessLevel.PRIVATE)
//public class FilmMapper {
//    public static Film mapToFilm(NewFilmRequest request) {
//        Film film = new Film();
//        film.setName(request.getName());
//        film.setDescription(request.getDescription());
//        film.setDuration(request.getDuration());
//        film.setReleaseDate(request.getReleaseDate());
//        return film;
//    }
//    public static FilmDto mapToFilmDto(Film film) {
//        FilmDto dto = new FilmDto();
//        dto.setId(film.getId());
//        dto.setDescription(film.getDescription());
//        dto.setLikes(film.getLikes());
//        dto.setName(film.getName());
//        dto.setReleaseDate(film.getReleaseDate());
//        return dto;
//    }
//
//    public static Film updateFilmFields(Film film, UpdateFilmRequest request) {
//        if (request.getDescription() != null) {
//            film.setDescription(request.getDescription());
//        }
//        if (request.getLikes() != null) {
//            film.setLikes(request.getLikes());
//        }
//        if (request.getName() != null && !request.getName().isBlank()) {
//            film.setName(request.getName());
//        }
//        if (request.getDuration() != null) {
//            film.setDuration(request.getDuration());
//        }
//        return film;
//    }
//}
