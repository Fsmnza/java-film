package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("film_id"));
        film.setName(rs.getString("film_name"));
        film.setDescription(rs.getString("film_description"));
        if (rs.getDate("film_release_date") != null)
            film.setReleaseDate(rs.getDate("film_release_date").toLocalDate());
        film.setDuration(rs.getInt("film_duration"));

        Rating mpa = new Rating();
        mpa.setId(rs.getInt("rating_id"));
        mpa.setName(rs.getString("rating_name"));
        film.setMpa(mpa);

        int genreId = rs.getInt("genre_id");
        if (genreId != 0) {
            Set<Genre> genres = new HashSet<>();
            Genre genre = new Genre();
            genre.setId(genreId);
            genre.setName(rs.getString("genre_name"));
            genres.add(genre);
            film.setGenres(genres);
        } else {
            film.setGenres(new HashSet<>());
        }

        int directorId = rs.getInt("director_id");
        if (directorId != 0) {
            Director director = new Director();
            director.setId(directorId);
            director.setName(rs.getString("director_name"));

            Set<Director> directors = new HashSet<>();
            directors.add(director);
            film.setDirectors(directors);
        } else {
            film.setDirectors(new HashSet<>());
        }
        film.setLikes(new HashSet<>());

        return film;
    }
}
