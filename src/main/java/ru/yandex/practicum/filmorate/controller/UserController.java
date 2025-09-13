package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public UserController(UserService userService, InMemoryUserStorage inMemoryUserStorage) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        return userService.getAllUsers();
    }


    @PostMapping
    public User create(@RequestBody User user) {
        return userService.addUser(user);

    }

    @PutMapping
    public User update(@RequestBody User user) {
        return userService.updateUser(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable("id") int userId, @PathVariable int friendId) {
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable("id") int userId, @PathVariable int friendId) {
        userService.removeFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<Map<String, Long>> friendsList(@PathVariable("id") int userId) {
        ArrayList<Long> ids = userService.getFriendsList(userId);
        return ids.stream()
                .map(fid -> Map.of("id", fid))
                .toList();
    }


    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> friendsListSharedWithOthers(@PathVariable("id") int userId, @PathVariable("otherId") int otherId) {
        return userService.getListOfMutualFriends(userId, otherId);
    }


}