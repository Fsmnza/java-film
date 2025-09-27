package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable int id) {
        return userService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody NewUserRequest request) {
        return userService.create(request);
    }

    @PutMapping
    public UserDto update(@Valid @RequestBody UpdateUserRequest request) {
        return userService.update(request);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable int id,
                          @PathVariable int friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable int id,
                             @PathVariable int friendId) {
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<UserDto> getFriends(@PathVariable int id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<UserDto> getCommonFriends(@PathVariable int id,
                                          @PathVariable int otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @GetMapping("/{id}/feed")
    public List<FeedDto> getFeed(@PathVariable int id) {
        return userService.getFeed(id);
    }

    @GetMapping("/{id}/recommendations")
    public List<FilmDto> getRecommendations(@PathVariable int id) {
        return userService.getRecommendations(id);
    }

}
