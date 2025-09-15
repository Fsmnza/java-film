package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public User addUser(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteUser(int id) {
        users.remove(id);
    }

    @Override
    public User getUserById(int id) {
        return users.get(id);
    }

    @Override
    public List<User> getAllUser() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void addFriend(int userId, int friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);

        if (user == null || friend == null) {
            throw new NotFoundException("Один из пользователей не найден");
        }

        user.getFriends().add((long) friendId);
        friend.getFriends().add((long) userId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);

        if (user == null || friend == null) {
            throw new NotFoundException("Один из пользователей не найден");
        }

        user.getFriends().remove((long) friendId);
        friend.getFriends().remove((long) userId);
    }

    @Override
    public List<User> getFriends(int userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        return user.getFriends().stream()
                .map(id -> users.get(Math.toIntExact(id)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getMutualFriends(int userId, int otherId) {
        User user = users.get(userId);
        User other = users.get(otherId);

        if (user == null || other == null) {
            throw new NotFoundException("Один из пользователей не найден");
        }

        Set<Long> mutual = new HashSet<>(user.getFriends());
        mutual.retainAll(other.getFriends());

        return mutual.stream()
                .map(id -> users.get(Math.toIntExact(id)))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
