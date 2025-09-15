package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RatingDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public Optional<Rating> getRatingById(int id) {
        String sql = "SELECT * FROM rating WHERE id = ?";
        return jdbcTemplate.query(sql, this::mapRowToRating, id)
                .stream()
                .findFirst();
    }

    public List<Rating> getAllRatings() {
        String sql = "SELECT * FROM rating";
        return jdbcTemplate.query(sql, this::mapRowToRating);
    }

    private Rating mapRowToRating(ResultSet rs, int rowNum) throws SQLException {
        return new Rating(
                rs.getInt("id"),
                rs.getString("name")
        );
    }
}
