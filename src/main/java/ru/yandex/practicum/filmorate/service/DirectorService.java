package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.mappers.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DirectorService {
    private final DirectorRepository directorRepository;

    public DirectorService(DirectorRepository directorRepository) {
        this.directorRepository = directorRepository;
    }

    public List<DirectorDto> getAllDirectors() {
        return directorRepository.getAllDirectors().stream()
                .map(DirectorMapper::toDto)
                .collect(Collectors.toList());
    }

    public DirectorDto getDirector(Integer id) {
        return DirectorMapper.toDto(directorRepository.getDirector(id));
    }

    public DirectorDto addDirector(Director director) {
        return DirectorMapper.toDto(directorRepository.addDirector(director));
    }

    public DirectorDto updateDirector(Director director) {
        return DirectorMapper.toDto(directorRepository.updateDirector(director));
    }

    public void deleteDirector(Integer id) {
        directorRepository.deleteDirector(id);
    }
}
