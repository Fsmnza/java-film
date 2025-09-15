package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
            rs.getInt("id"),
            rs.getString("email"),
            rs.getString("login"),
            rs.getString("name"),
            rs.getDate("birthday").toLocalDate()
    );

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void addFriend(int userId, int friendId) {
        String sql = "INSERT INTO friends(user_id, friend_id, status) VALUES (?, ?, 'pending')";
        jdbc.update(sql, userId, friendId);
    }

    public void confirmFriend(int userId, int friendId) {
        String sql = "UPDATE friends SET status='confirmed' WHERE user_id=? AND friend_id=?";
        jdbc.update(sql,  userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM friends WHERE (user_id=? AND friend_id=?) OR (user_id=? AND friend_id=?)";
        jdbc.update(sql, userId, friendId, friendId, userId);
    }

    public List<User> getFriendsList(int userId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friends f ON u.id = f.friend_id " +
                "WHERE f.user_id=? AND f.status='confirmed'";
        return jdbc.query(sql, userRowMapper, userId);
    }

    public List<User> getMutualFriends(int userId, int otherId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friends f1 ON u.id = f1.friend_id " +
                "JOIN friends f2 ON u.id = f2.friend_id " +
                "WHERE f1.user_id=? AND f1.status='confirmed' " +
                "AND f2.user_id=? AND f2.status='confirmed'";
        return jdbc.query(sql, userRowMapper, userId, otherId);
    }
}