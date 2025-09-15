package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;

@Repository
@Qualifier("dbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbc;

    public FilmDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private Film mapFilm(ResultSet rs) throws SQLException {
        int ratingId = rs.getInt("rating_id");
        Rating rating = jdbc.queryForObject(
                "SELECT id, name FROM rating WHERE id = ?",
                (r, rowNum) -> new Rating(r.getInt("id"), r.getString("name")),
                ratingId
        );

        return new Film(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDate("releaseDate") != null ? rs.getDate("releaseDate").toLocalDate() : null,
                rs.getInt("duration"),
                new HashSet<>(),
                rating
        );
    }

    @Override
    public Film addFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO film (name, description, releaseDate, duration, rating_id) VALUES (?, ?, ?, ?, ?)";

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            if (film.getReleaseDate() != null) {
                ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            } else {
                ps.setNull(3, java.sql.Types.DATE);
            }
            ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getRating() != null ? film.getRating().getId() : null);
            return ps;
        }, keyHolder);

        int filmId = keyHolder.getKey().intValue();
        film.setId(filmId);
        if (film.getGenres() != null) {
            String sqlGenre = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbc.update(sqlGenre, filmId, genre.getId());
            }
        }

        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE film SET name = ?, description = ?, releaseDate = ?, duration = ?, rating_id = ? WHERE id = ?";
        jdbc.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
                film.getDuration(),
                film.getRating() != null ? film.getRating().getId() : null,
                film.getId());
        return film;
    }

    @Override
    public void deleteFilm(int id) {
        String sql = "DELETE FROM film WHERE id = ?";
        jdbc.update(sql, id);
    }

    @Override
    public Film getFilmById(int id) {
        String sql = "SELECT * FROM film WHERE id = ?";
        return jdbc.query(sql, (rs, rowNum) -> mapFilm(rs), id)
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.duration,
                       f.releaseDate,
                       f.rating_id,
                       r.name AS rating_name
                FROM film f
                LEFT JOIN rating r ON f.rating_id = r.id
                """;
        return jdbc.query(sql, (rs, rowNum) -> {
            Rating rating = null;
            if (rs.getObject("rating_id") != null) {
                rating = new Rating(
                        rs.getInt("rating_id"),
                        rs.getString("rating_name")
                );
            }
            Film film = new Film();
            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setDuration(rs.getInt("duration"));
            film.setReleaseDate(
                    rs.getDate("releaseDate") != null ? rs.getDate("releaseDate").toLocalDate() : null
            );
            film.setRating(rating);
            return film;
        });
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.duration,
                       f.releaseDate,
                       f.rating_id,
                       r.name AS rating_name,
                       COUNT(l.user_id) AS like_count
                FROM film f
                LEFT JOIN rating r ON f.rating_id = r.id
                LEFT JOIN likes l ON f.id = l.film_id
                GROUP BY f.id, f.name, f.description, f.duration, f.releaseDate, f.rating_id, r.name
                ORDER BY like_count DESC
                LIMIT ?
                """;
        return jdbc.query(sql, (rs, rowNum) -> {
            Rating rating = null;
            if (rs.getObject("rating_id") != null) {
                rating = new Rating(
                        rs.getInt("rating_id"),
                        rs.getString("rating_name")
                );
            }
            Film film = new Film();
            film.setId(rs.getInt("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setDuration(rs.getInt("duration"));
            if (rs.getDate("releaseDate") != null) {
                film.setReleaseDate(rs.getDate("releaseDate").toLocalDate());
            }
            film.setRating(rating);

            film.setLikes(new HashSet<>());
            return film;
        }, count);
    }


}
