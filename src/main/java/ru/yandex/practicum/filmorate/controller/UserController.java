package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }


    @PostMapping
    public User create(@RequestBody User user) {
        validateUser(user);
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        validateUser(user);
        if (!users.containsKey(user.getId())) {
            log.info("Попытка найти пользователя с таким id={} провалилась", user.getId());
            throw new IllegalArgumentException("Пользователь с id " + user.getId() + " не существует");
        }
        users.put(user.getId(), user);
        return user;
    }

    private void validateUser(User user) {
        if (user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new IllegalArgumentException("Электронная почта не может быть пустой или должна содержать символ @");
        }
        if (user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new IllegalArgumentException("Логин не может быть пустым или содержать пробелы");
        }
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() > LocalDate.now().getYear()) {
            throw new IllegalArgumentException("Дата рождения не может быть в будущем");
        }
    }
}