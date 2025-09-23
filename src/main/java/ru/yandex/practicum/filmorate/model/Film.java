package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class Film {
    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
    private Rating mpa;
    private List<Genre> genres = new ArrayList<>();
    private List<Director> directors = new ArrayList<>();
    private Set<Integer> likes = new HashSet<>();
}
