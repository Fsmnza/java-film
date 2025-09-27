package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("film_id"));
        film.setName(rs.getString("film_name"));
        film.setDescription(rs.getString("film_description"));
        film.setReleaseDate(rs.getDate("film_release_date").toLocalDate());
        film.setDuration(rs.getInt("film_duration"));
        Rating rating = new Rating();
        rating.setId(rs.getInt("rating_id"));
        rating.setName(rs.getString("rating_name"));
        film.setMpa(rating);
        int directorId = rs.getInt("director_id");
        if (directorId > 0) {
            Director director = new Director();
            director.setId(directorId);
            director.setName(rs.getString("director_name"));
            film.getDirectors().add(director);
        }
        return film;
    }
}
