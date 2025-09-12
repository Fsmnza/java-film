package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
class FilmorateApplicationTests {
    private final UserController userController = new UserController();
    private final FilmController filmController = new FilmController();

    @Test
    void shouldSetLoginAsNameIfNameIsBlank() {
        User user = new User();
        user.setEmail("malika@mail.com");
        user.setLogin("Mako");
        user.setName("");
        user.setBirthday((12));

        User created = userController.create(user);

        assertThat(created.getName()).isEqualTo("Mako");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("конфета");
        film.setDescription("вкусная");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-100);

        assertThatThrownBy(() -> filmController.create(film))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Продолжительность фильма должна быть положительным числом");
    }
}

