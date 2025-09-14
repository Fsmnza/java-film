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
class FilmControllerUpdateTests {

    @Autowired
    private FilmController filmController;

    @Test
    void shouldUpdateFilm() {
        Film film = new Film();
        film.setId(1);
        film.setName("Ван пис");
        film.setDescription("Аниме");
        film.setReleaseDate(LocalDate.of(2000, 3, 23));
        film.setDuration(120);

        filmController.create(film);
        film.setName("Наруто");
        film.setDescription("Другое аниме");
        film.setDuration(136);

        ResponseEntity<Film> response = filmController.update(film);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("Наруто");
    }

    @Autowired
    private UserController userController;

    @Test
    void shouldUpdateUser() {
        User user = new User();
        user.setId(1);
        user.setEmail("mako@mail.com");
        user.setLogin("Mk");
        user.setName("Malika");
        user.setBirthday(LocalDate.of(2005, 8, 11));

        userController.create(user);

        user.setLogin("neFsmnza");
        user.setName("neMalika");

        ResponseEntity<User> response = userController.update(user);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getLogin()).isEqualTo("neFsmnza");
        assertThat(response.getBody().getName()).isEqualTo("neMalika");
    }
}