package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class UserService {
    private final UserStorage userStorage;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        if (user == null) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        if (friend == null) throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        user.getFriends().add((long) friendId);
        friend.getFriends().add((long) userId);
        userStorage.updateUser(user);
        userStorage.updateUser(friend);
        log.info("Пользователь {} друзья: {}", userId, user.getFriends());
        log.info("Пользователь {} друзья: {}", friendId, friend.getFriends());
    }

    public void removeFriend(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        if (user == null) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        if (friend == null) throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        user.getFriends().remove((long) friendId);
        friend.getFriends().remove((long) userId);
    }

    public List<User> getListOfMutualFriends(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        if (user == null) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        if (friend == null) throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        Set<Long> commonIds = new HashSet<>(user.getFriends());
        commonIds.retainAll(friend.getFriends());
        return commonIds.stream()
                .map(id -> userStorage.getUserById(id.intValue()))
                .collect(Collectors.toList());
    }

    public ArrayList<Long> getFriendsList(int userId) {
        User user = userStorage.getUserById(userId);
        if (user == null) throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        return new ArrayList<>(user.getFriends());
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        User user1 = userStorage.getUserById(user.getId());
        if (user1 == null) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        user1.setName(user.getName());
        user1.setEmail(user.getEmail());
        user1.setLogin(user.getLogin());
        user1.setBirthday(user.getBirthday());
        userStorage.updateUser(user1);
        return user1;
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUser();
    }
}
