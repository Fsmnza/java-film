package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Review {
    private Integer reviewId;
    private String content;
    private Boolean isPositive;
    private Integer userId;
    private Integer filmId;
    private Integer useful = 0; // по умолчанию 0
}
