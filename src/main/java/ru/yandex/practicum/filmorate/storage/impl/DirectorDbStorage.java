package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    @Override
    public List<Director> findAll() {
        String sql = "SELECT * FROM directors";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Director(rs.getInt("director_id"), rs.getString("name")));
    }

    @Override
    public Optional<Director> findById(int id) {
        String sql = "SELECT * FROM directors WHERE director_id = ?";
        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(sql,
                            (rs, rowNum) -> new Director(rs.getInt("director_id"), rs.getString("name")),
                            id)
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


    @Override
    public Director create(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"director_id"});
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        director.setId(keyHolder.getKey().intValue());
        return director;
    }

    @Override
    public Director update(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE director_id = ?";
        int rows = jdbcTemplate.update(sql, director.getName(), director.getId());
        if (rows == 0) {
            throw new NotFoundException("Director with id=" + director.getId() + " not found");
        }
        return director;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM directors WHERE director_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Film> getFilmsByDirectorSortedByLikes(int directorId) {
        String sql = """
                SELECT f.*, COUNT(fl.user_id) AS likes_count
                FROM films f
                JOIN film_directors fd ON f.film_id = fd.film_id
                LEFT JOIN film_likes fl ON f.film_id = fl.film_id
                WHERE fd.director_id = ?
                GROUP BY f.film_id
                ORDER BY likes_count DESC
                """;

        return jdbcTemplate.query(sql, filmRowMapper, directorId);
    }

    @Override
    public List<Film> getFilmsByDirectorSortedByYear(int directorId) {
        String sql = """
                SELECT f.*
                FROM films f
                JOIN film_directors fd ON f.film_id = fd.film_id
                WHERE fd.director_id = ?
                ORDER BY f.release_date
                """;

        return jdbcTemplate.query(sql, filmRowMapper, directorId);
    }

}
