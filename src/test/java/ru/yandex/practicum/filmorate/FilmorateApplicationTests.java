package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

    @Autowired
    private UserController userController;

    @Autowired
    private FilmController filmController;

    @Test
    void shouldSetLoginAsNameIfNameIsBlank() {
        User user = new User();
        user.setEmail("malika@mail.com");
        user.setLogin("Mako");
        user.setName("");
        user.setBirthday(LocalDate.of(2005, 9, 20));

        User created = userController.create(user);

        assertThat(created.getName()).isEqualTo("Mako");
    }

    @Test
    void shouldReturnBadRequestWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("конфета");
        film.setDescription("вкусная");
        film.setReleaseDate(LocalDate.of(2000, 11, 27));
        film.setDuration(-100);

        ResponseEntity<Film> response = filmController.create(film);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }
}