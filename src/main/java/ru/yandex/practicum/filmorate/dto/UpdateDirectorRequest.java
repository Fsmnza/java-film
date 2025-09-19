package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateDirectorRequest {
    @NotNull
    private Integer id;

    @NotBlank(message = "Имя режиссёра не должно быть пустым")
    private String name;

    public boolean hasId() {
        return id != null;
    }

    public boolean hasName() {
        return name != null;
    }
}
