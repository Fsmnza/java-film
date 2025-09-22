package ru.yandex.practicum.filmorate.dal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public class FoundRepository<T> {
    protected final JdbcTemplate jdbcTemplate;
    private final RowMapper<T> rowMapper;
    private static final Logger logger = LoggerFactory.getLogger(FoundRepository.class);

    public FoundRepository(JdbcTemplate jdbcTemplate, RowMapper<T> rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }

    protected Optional<T> findOne(String query, Object... params) {
        try {
            T result = jdbcTemplate.queryForObject(query, rowMapper, params);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    protected List<T> findMany(String query, Object... params) {
        return jdbcTemplate.query(query, rowMapper, params);
    }

    protected void update(String query, Object... params) {
        int rowsUpdate = jdbcTemplate.update(query, params);
        if (rowsUpdate == 0) {
            logger.warn("Не было обновлено ни одной строки");
        }
    }

    protected int insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        Integer id = keyHolder.getKeyAs(Integer.class);
        if (id != null) {
            return id;
        } else {
            throw new RuntimeException("Не удалось сохранить данные");
        }
    }

    protected void insertSimple(String query, Object... params) {
        int rows = jdbcTemplate.update(query, params);
        if (rows == 0) {
            logger.warn("Не было обновлено ни одной строки для запроса: {}", query);
        }
    }

    protected List<Integer> findManyInts(String query, Object... params) {
        return jdbcTemplate.query(query, (rs, rowNum) -> rs.getInt(1), params);
    }

    protected List<T> findMany(String query, ResultSetExtractor<List<T>> resultSetExtractor, Object... params) {
        return jdbcTemplate.query(query, resultSetExtractor, params);
    }

    protected Optional<T> findOne(String query, ResultSetExtractor<List<T>> resultSetExtractor, Object... params) {
        List<T> result = jdbcTemplate.query(query, resultSetExtractor, params);

        if (result != null && result.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(result.getFirst());
        }
    }
}