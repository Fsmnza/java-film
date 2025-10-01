package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateReviewRequest {
    @NotNull(message = "Не указан id отзыва")
    private Integer reviewId;

    @Pattern(regexp = ".+", message = "Текст отзыва не может быть пустым")
    private String content;

    private Boolean isPositive;

    public boolean hasContent() {
        return content != null;
    }

    public boolean hasIsPositive() {
        return isPositive != null;
    }
}