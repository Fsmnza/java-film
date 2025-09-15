package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
@Qualifier("dbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;

    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper userRowMapper) {
        this.jdbc = jdbc;
        this.userRowMapper = userRowMapper;
    }

    @Override
    public User addUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null);
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbc.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null,
                user.getId());
        return user;
    }

    @Override
    public void deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbc.update(sql, id);
    }

    @Override
    public User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbc.query(sql, userRowMapper, id).stream().findFirst().orElse(null);
    }

    @Override
    public List<User> getAllUser() {
        String sql = "SELECT * FROM users";
        return jdbc.query(sql, userRowMapper);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        String sql = "INSERT INTO friends(user_id, friend_id) VALUES (?, ?)";
        jdbc.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbc.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(int userId) {
        String sql = """
                    SELECT u.* FROM users u
                    JOIN friends f ON u.id = f.friend_id
                    WHERE f.user_id = ?
                """;
        return jdbc.query(sql, userRowMapper, userId);
    }

    @Override
    public List<User> getMutualFriends(int userId, int otherId) {
        String sql = """
                    SELECT u.* FROM users u
                    JOIN friends f1 ON u.id = f1.friend_id AND f1.user_id = ?
                    JOIN friends f2 ON u.id = f2.friend_id AND f2.user_id = ?
                """;
        return jdbc.query(sql, userRowMapper, userId, otherId);
    }
}
