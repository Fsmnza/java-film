package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@Validated
public class FilmController {
    private final FilmService filmService;
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос: ");
        return filmService.findAllFilms();
    }

    @PostMapping
    public ResponseEntity<Film> create(@Valid @RequestBody Film film) {
        Film newCreated = filmService.createFilm(film);
        log.info("Фильм создан: {}", newCreated);
        return ResponseEntity.ok(newCreated);
    }

    @PutMapping
    public ResponseEntity<Film> update(@RequestBody Film film) {
        Film updatedFilm = filmService.updateFilm(film);
        log.info("Фильм обновлен: {}", updatedFilm);
        return ResponseEntity.ok(updatedFilm);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        log.info("Получены 10 популярных фильмов по лайкам: ");
        return filmService.getPopularFilmsByLikes(count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") int filmId, @PathVariable("userId") int userId) {
        log.info("Получен запрос по добавлению: фильм{}/лайк от пользователя{}", filmId, userId);
        filmService.addLike(userId, filmId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable("id") int filmId, @PathVariable("userId") int userId) {
        log.info("Получен запрос по удалению: фильм{}/лайк от пользователя{}", filmId, userId);
        filmService.removeLIke(userId, filmId);
    }
}
