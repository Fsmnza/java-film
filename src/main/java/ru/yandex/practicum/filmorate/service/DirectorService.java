package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorRepository directorRepository;

    public List<Director> getAll() {
        return directorRepository.findAll();
    }

    public Optional<Director> getById(int id) {
        return directorRepository.findById(id);
    }

    public Director create(Director director) {
        return directorRepository.create(director);
    }

    public Director update(Director director) {
        Director existingDirector = directorRepository.findById(director.getId())
                .orElseThrow(() -> new NotFoundException(
                        "Режиссёр с id = " + director.getId() + " не найден"
                ));
        existingDirector.setName(director.getName());
        return directorRepository.update(existingDirector);
    }

    public void delete(int id) {
        if (directorRepository.findById(id).isEmpty()) {
            throw new NotFoundException("Режиссёр с id = " + id + " не найден");
        }
        directorRepository.delete(id);
    }
}