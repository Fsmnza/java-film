package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.List;
import java.util.Optional;

@Repository
public class RatingRepository extends FoundRepository<Rating> {
    private static final String TABLE_NAME = "ratings";
    private static final String FIND_ALL_QUERY = "SELECT * FROM " + TABLE_NAME;
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM " + TABLE_NAME + " WHERE rating_id = ?";

    @Autowired
    public RatingRepository(JdbcTemplate jdbcTemplate, RowMapper<Rating> rowMapper) {
        super(jdbcTemplate, rowMapper);
    }

    public List<Rating> getAll() {
        log.debug("Запрос на получение всех строк таблицы ratings");
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Rating> getById(int mpaId) {
        log.debug("Запрос на получение строки таблицы ratings с id = {}", mpaId);
        return findOne(FIND_BY_ID_QUERY, mpaId);
    }

    public Rating getRatingById(int mpaId) {
        String query = "SELECT rating_id, name FROM ratings WHERE rating_id = ?";
        return jdbcTemplate.queryForObject(query, (rs, rowNum) ->
                new Rating(rs.getInt("rating_id"), rs.getString("name")), mpaId);
    }

}
