package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
public class FilmRepository {

    private final JdbcTemplate jdbc;

    public FilmRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Rating rating = null;
        if (rs.getObject("rating_id") != null) {
            rating = new Rating(
                    rs.getInt("rating_id"),
                    rs.getString("rating_name")
            );
        }

        Set<Genre> genres = new HashSet<>();

        return new Film(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("releaseDate").toLocalDate(),
                rs.getInt("duration"),
                genres,
                rating
        );
    };

    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO likes(film_id, user_id) VALUES (?, ?)";
        jdbc.update(sql, filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbc.update(sql, filmId, userId);
    }

    public List<Film> getPopularFilms(int limit) {
        String sql = "SELECT f.*, r.name AS rating_name, f.rating_id, COUNT(l.user_id) AS like_count " +
                "FROM film f " +
                "LEFT JOIN rating r ON f.rating_id = r.id " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id, r.name, f.rating_id " +
                "ORDER BY like_count DESC " +
                "LIMIT ?";
        return jdbc.query(sql, filmRowMapper, limit);
    }
}
