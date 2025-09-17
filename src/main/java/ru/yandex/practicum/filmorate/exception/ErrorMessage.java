package ru.yandex.practicum.filmorate.exception;

import lombok.Data;
import lombok.NonNull;

@Data
public class ErrorMessage {
    @NonNull
    private String message;
}
