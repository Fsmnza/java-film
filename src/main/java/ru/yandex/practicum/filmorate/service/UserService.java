package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("dbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        if (userStorage.getUserById(user.getId()) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return userStorage.updateUser(user);
    }

    public User getUserById(int id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        return user;
    }

    public List<User> getAllUser() {
        return userStorage.getAllUser();
    }

    public void deleteUser(int id) {
        if (userStorage.getUserById(id) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        userStorage.deleteUser(id);
    }

    public void addFriend(int userId, int friendId) {
        validateUsers(userId, friendId);
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        validateUsers(userId, friendId);
        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(int userId) {
        if (userStorage.getUserById(userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        return userStorage.getFriends(userId);
    }

    public List<User> getMutualFriends(int userId, int otherId) {
        validateUsers(userId, otherId);
        return userStorage.getMutualFriends(userId, otherId);
    }

    private void validateUsers(int userId, int friendId) {
        if (userStorage.getUserById(userId) == null || userStorage.getUserById(friendId) == null) {
            throw new NotFoundException("Один из пользователей не найден");
        }
    }
}
