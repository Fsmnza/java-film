package ru.yandex.practicum.filmorate.dal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(RatingRepository.class);

    @Autowired
    public RatingRepository(JdbcTemplate jdbcTemplate, RowMapper<Rating> rowMapper) {
        super(jdbcTemplate, rowMapper);
    }

    public List<Rating> getAll() {
        logger.debug("Запрос на получение всех строк таблицы ratings");
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Rating> getById(int mpaId) {
        logger.debug("Запрос на получение строки таблицы ratings с id = {}", mpaId);
        return findOne(FIND_BY_ID_QUERY, mpaId);
    }
}
