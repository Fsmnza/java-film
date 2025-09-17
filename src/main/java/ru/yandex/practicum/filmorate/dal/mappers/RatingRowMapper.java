package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class RatingRowMapper implements RowMapper<Rating> {
    @Override
    public Rating mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Rating mpaRating = new Rating();
        mpaRating.setId(resultSet.getInt("rating_id"));
        mpaRating.setName(resultSet.getString("name"));

        return mpaRating;
    }
}
