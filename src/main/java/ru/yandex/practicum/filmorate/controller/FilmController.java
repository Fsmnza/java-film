package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.service.FilmService;

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
    public List<FilmDto> getAll() {
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    public FilmDto getById(@PathVariable int id) {
        return filmService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto create(@Valid @RequestBody NewFilmRequest request) {
        return filmService.create(request);
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody UpdateFilmRequest request) {
        return filmService.update(request);
    }

    @PutMapping("/{id}/like/{userId}")
    public void putLike(@PathVariable int id,
                        @PathVariable int userId) {
        filmService.putLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id,
                           @PathVariable int userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getPopular(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopular(count);
    }

    @GetMapping("/director/{id}")
    public List<FilmDto> getFilmsByDirector(
            @PathVariable Integer id,
            @RequestParam(name = "sortBy") String sortBy) {
        return switch (sortBy) {
            case "likes" -> filmService.getFilmsByDirectorSortedByLikes(id);
            case "year" -> filmService.getFilmsByDirectorSortedByYear(id);
            default -> throw new IllegalArgumentException("Некорректный параметр sortBy :" + sortBy);
        };
    }
}
