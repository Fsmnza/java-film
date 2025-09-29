package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
public class FoundFilmRepository implements ResultSetExtractor<List<Film>> {

    @Override
    public List<Film> extractData(ResultSet rs) throws SQLException, DataAccessException {
        final Map<Integer, Film> filmMap = new LinkedHashMap<>();

        while (rs.next()) {
            Integer filmId = rs.getInt("film_id");
            Film film = filmMap.get(filmId);
            if (film == null) {
                film = new Film();
                film.setId(filmId);
                film.setName(rs.getString("film_name"));
                film.setDescription(rs.getString("film_description"));
                film.setReleaseDate(rs.getDate("film_release_date").toLocalDate());
                film.setDuration(rs.getInt("film_duration"));
                Rating mpaRating = new Rating();
                mpaRating.setId(rs.getInt("rating_id"));
                mpaRating.setName(rs.getString("rating_name"));
                film.setMpa(mpaRating);
                film.setGenres(new ArrayList<>());
                film.setDirectors(new ArrayList<>());
                filmMap.put(filmId, film);
            }
            int genreId = rs.getInt("genre_id");
            if (!rs.wasNull()) {
                Genre genre = new Genre();
                genre.setId(genreId);
                genre.setName(rs.getString("genre_name"));
                if (!film.getGenres().contains(genre)) {
                    film.getGenres().add(genre);
                }
            }
            int directorId = rs.getInt("director_id");
            if (!rs.wasNull()) {
                Director director = new Director();
                director.setId(directorId);
                director.setName(rs.getString("director_name"));
                if (!film.getDirectors().contains(director)) {
                    film.getDirectors().add(director);
                }
            }
        }
        return new ArrayList<>(filmMap.values());
    }
}
