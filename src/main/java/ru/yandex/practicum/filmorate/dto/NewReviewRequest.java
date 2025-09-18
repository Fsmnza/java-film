package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NewReviewRequest {
    @NotBlank(message = "Текст отзыва не может быть пустым")
    private String content;

    @NotNull(message = "Тип отзыва должен быть указан")
    private Boolean isPositive;

    @NotNull(message = "Пользователь должен быть указан")
    private Integer userId;

    @NotNull(message = "Фильм должен быть указан")
    private Integer filmId;
}