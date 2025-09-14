package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService, InMemoryUserStorage inMemoryUserStorage) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос найти всех пользователей: ");
        return userService.getAllUsers();
    }


    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        User newCreated = userService.addUser(user);
        log.info("Пользователь создан: {}", newCreated);
        return ResponseEntity.ok(newCreated);

    }

    @PutMapping
    public ResponseEntity<User> update(@RequestBody User user) {
        User updatedUser = userService.updateUser(user);
        log.info("Пользователь обновлен: {}", updatedUser);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable("id") int userId, @PathVariable int friendId) {
        log.info("Получен запрос на добавление в друзья: от{}/к{}", userId, friendId);
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable("id") int userId, @PathVariable int friendId) {
        log.info("Получен запрос на удаление из друзей: от{}/к{}", userId, friendId);
        userService.removeFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<Map<String, Long>> friendsList(@PathVariable("id") int userId) {
        ArrayList<Long> ids = userService.getFriendsList(userId);
        log.info("Получен запрос на выведение всех друзей: ");
        return ids.stream()
                .map(fid -> Map.of("id", fid))
                .toList();
    }


    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> friendsListSharedWithOthers(@PathVariable("id") int userId, @PathVariable("otherId") int otherId) {
        log.info("Получен запрос на выведение общих друзей: у {}", userId);
        return userService.getListOfMutualFriends(userId, otherId);
    }


}