package ru.yandex.practicum.filmorate.mappers;

import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.model.Director;

public class DirectorMapper {
    public static DirectorDto toDto(Director director) {
        DirectorDto dto = new DirectorDto();
        dto.setId(director.getId());
        dto.setName(director.getName());
        return dto;
    }

    public static Director fromDto(DirectorDto dto) {
        Director director = new Director();
        director.setId(dto.getId());
        director.setName(dto.getName());
        return director;
    }

    public static Director fromUpdateRequest(UpdateDirectorRequest request) {
        Director director = new Director();
        if (request.hasId()) {
            director.setId(request.getId());
        }
        if (request.hasName()) {
            director.setName(request.getName());
        }
        return director;
    }

}
