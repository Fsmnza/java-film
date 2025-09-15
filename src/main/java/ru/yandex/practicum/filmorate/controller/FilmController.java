package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/films")
@Validated
public class FilmController {
    private final FilmService filmService;
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<FilmDto> findAll() {
        log.info("Получен запрос: ");
        return filmService.findAllFilms();
    }

    @PostMapping
    public ResponseEntity<FilmDto> create(@Valid @RequestBody NewFilmRequest request) {
        log.info("Фильм создан: {}", request);
        return ResponseEntity.ok(filmService.createFilm(request));
    }

    @PutMapping
    public ResponseEntity<FilmDto> update(@RequestBody UpdateFilmRequest request) {
        log.info("Фильм обновлен: {}", request);
        return ResponseEntity.ok(filmService.updateFilm(request));
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int userId, @PathVariable int filmId) {
        log.info("Получен запрос по добавлению: фильм{}/лайк от пользователя{}", filmId, userId);
        filmService.addLike(userId, filmId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int userId, @PathVariable int filmId) {
        log.info("Получен запрос по удалению: фильм{}/лайк от пользователя{}", filmId, userId);
        filmService.removeLike(userId, filmId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopular(@RequestParam(defaultValue = "10") int count) {
        log.info("Получены 10 популярных фильмов по лайкам: ");
        return filmService.getPopularFilmsByLikes(count);
    }
}
