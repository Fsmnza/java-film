package ru.yandex.practicum.filmorate.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class GenreDbStorage {
    private final JdbcTemplate jdbc;

    public GenreDbStorage(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Genre> getGenreById(int id) {
        String sql = "SELECT * FROM genre WHERE id = ?";
        return jdbc.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("id"), rs.getString("name")), id
        ).stream().findFirst();
    }

    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genre";
        return jdbc.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("id"), rs.getString("name")));
    }

    public Set<Genre> getGenresByFilmId(int filmId) {
        String sql = "SELECT g.id, g.name " +
                "FROM film_genre fg " +
                "JOIN genre g ON fg.genre_id = g.id " +
                "WHERE fg.film_id = ?";
        return new HashSet<>(jdbc.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("id"), rs.getString("name")), filmId));
    }

    public void saveFilmGenres(int filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : genres) {
            jdbc.update(sql, filmId, genre.getId());
        }
    }

    public void deleteFilmGenres(int filmId) {
        String sql = "DELETE FROM film_genre WHERE film_id = ?";
        jdbc.update(sql, filmId);
    }
}
