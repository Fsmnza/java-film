package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.mappers.DirectorMapper;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
public class DirectorController {
    private final DirectorService directorService;

    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping
    public List<DirectorDto> getAll() {
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public DirectorDto getById(@PathVariable Integer id) {
        return directorService.getDirector(id);
    }

    @PostMapping
    public DirectorDto create(@RequestBody @Valid DirectorDto dto) {
        return directorService.addDirector(DirectorMapper.fromDto(dto));
    }

    @PutMapping
    public DirectorDto update(@RequestBody @Valid UpdateDirectorRequest request) {
        return directorService.updateDirector(DirectorMapper.fromUpdateRequest(request));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        directorService.deleteDirector(id);
    }
}
