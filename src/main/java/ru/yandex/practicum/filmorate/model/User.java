package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = {"email"})
public class User {
    private Integer id;
    @NotBlank(message = "Электронная почта не может быть пустой")
    @Pattern(regexp = "^\\S+$", message = "Электронная почта не может содержать пробелы")
    @Email(message = "Электронная почта не соответствует формату")
    private String email;
    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелы")
    private String login;
    private String name;
    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
