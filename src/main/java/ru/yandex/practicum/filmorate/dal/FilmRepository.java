package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class FilmRepository {

    private final JdbcTemplate jdbc;

    public FilmRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Film> filmRowMapper = (ResultSet rs, int rowNum) -> {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        if (rs.getDate("releaseDate") != null) {
            film.setReleaseDate(rs.getDate("releaseDate").toLocalDate());
        }
        film.setDuration(rs.getInt("duration"));
        film.setRating(Rating.valueOf(rs.getString("rating")));
        return film;
    };

    public List<Film> findAll() {
        String sql = "SELECT * FROM film";
        return jdbc.query(sql, filmRowMapper);
    }

    public Film save(Film film) {
        String sql = "INSERT INTO film(name, description, releaseDate, duration, rating) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id";
        int id = jdbc.queryForObject(sql,
                (rs, rowNum) -> rs.getInt("id"),
                film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getRating());
        film.setId(id);
        return film;
    }

    public Film update(Film film) {
        String sql = "UPDATE film SET name=?, description=?, releaseDate=?, duration=?, rating=? WHERE id=?";
        jdbc.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getRating(), film.getId());
        return film;
    }

    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO likes(film_id, user_id) VALUES (?, ?)";
        jdbc.update(sql, filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM likes WHERE film_id=? AND user_id=?";
        jdbc.update(sql, filmId, userId);
    }

    public List<Film> getPopularFilms(int limit) {
        String sql = "SELECT f.*, COUNT(l.user_id) AS like_count FROM film f " +
                "LEFT JOIN likes l ON f.id=l.film_id " +
                "GROUP BY f.id ORDER BY like_count DESC LIMIT ?";
        return jdbc.query(sql, filmRowMapper, limit);
    }
}
