package ru.yandex.practicum.filmorate.dto;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Rating;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
@Data
public class FilmDto {
    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private Set<Long> likes = new HashSet<>();
    private Rating rating;
}
