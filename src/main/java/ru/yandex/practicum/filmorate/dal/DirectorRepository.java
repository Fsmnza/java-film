package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Repository
public class DirectorRepository {
    private final JdbcTemplate jdbcTemplate;

    public DirectorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Director> getAllDirectors() {
        String sql = "SELECT * FROM directors";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Director(
                        rs.getInt("director_id"),
                        rs.getString("name")
                )
        );
    }

    public Director getDirector(int id) {
        String sql = "SELECT * FROM directors WHERE director_id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                new Director(
                        rs.getInt("director_id"),
                        rs.getString("name")
                ), id
        );
    }


    public Director addDirector(Director director) {
        String sql = "INSERT INTO directors(name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);
        director.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return director;
    }

    public Director updateDirector(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE director_id = ?";
        jdbcTemplate.update(sql, director.getName(), director.getId());
        return director;
    }

    public void deleteDirector(int id) {
        String sql = "DELETE FROM directors WHERE director_id = ?";
        jdbcTemplate.update(sql, id);
    }
}
