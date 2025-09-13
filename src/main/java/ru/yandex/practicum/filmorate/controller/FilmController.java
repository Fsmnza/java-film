package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAllFilms();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilmsByLikes(count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") int filmId, @PathVariable("userId") int userId) {
        filmService.addLike(userId, filmId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable("id") int filmId, @PathVariable("userId") int userId) {
        filmService.removeLIke(userId, filmId);
    }
}
