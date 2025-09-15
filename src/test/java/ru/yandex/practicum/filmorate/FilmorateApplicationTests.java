package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.GenreDbStorage;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.RatingDbStorage;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class FilmorateApplicationTests {
    private FilmStorage filmStorage;
    private FilmRepository filmRepository;
    private RatingDbStorage ratingDbStorage;
    private GenreDbStorage genreDbStorage;
    private FilmService filmService;

    @BeforeEach
    public void setUp() {
        filmStorage = mock(FilmStorage.class);
        filmRepository = mock(FilmRepository.class);
        ratingDbStorage = mock(RatingDbStorage.class);
        genreDbStorage = mock(GenreDbStorage.class);
        filmService = new FilmService(filmStorage, filmRepository, ratingDbStorage, genreDbStorage);
    }

    @Test
    public void testGetAllFilms() {
        Film film1 = new Film();
        film1.setId(1);
        film1.setName("Film 1");
        film1.setGenres(Set.of(new Genre(1, "Genre 1")));

        Film film2 = new Film();
        film2.setId(2);
        film2.setName("Film 2");
        film2.setGenres(Set.of(new Genre(2, "Genre 2")));

        when(filmStorage.getAllFilms()).thenReturn(List.of(film1, film2));
        when(genreDbStorage.getGenresByFilmId(1)).thenReturn(Set.of(new Genre(1, "Genre 1")));
        when(genreDbStorage.getGenresByFilmId(2)).thenReturn(Set.of(new Genre(2, "Genre 2")));

        List<Film> films = filmService.getAllFilms();

        assertEquals(2, films.size());
        assertTrue(films.stream().anyMatch(f -> f.getName().equals("Film 1")));
        assertTrue(films.stream().anyMatch(f -> f.getName().equals("Film 2")));
    }
}