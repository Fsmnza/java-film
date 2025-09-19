package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
public class FoundFilmRepository implements ResultSetExtractor<List<Film>> {

    @Override
    public List<Film> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
        Map<Integer, Film> filmMap = new LinkedHashMap<>();

        while (resultSet.next()) {
            int filmId = resultSet.getInt("film_id");

            Film film = filmMap.get(filmId);
            if (film == null) {
                film = new Film();
                film.setId(filmId);
                film.setName(resultSet.getString("film_name"));
                film.setDescription(resultSet.getString("film_description"));
                film.setReleaseDate(resultSet.getDate("film_release_date").toLocalDate());
                film.setDuration(resultSet.getInt("film_duration"));

                Rating rating = new Rating();
                rating.setId(resultSet.getInt("rating_id"));
                rating.setName(resultSet.getString("rating_name"));
                film.setMpa(rating);

                film.setGenres(new HashSet<>());
                film.setDirectors(new HashSet<>());
                filmMap.put(filmId, film);
            }
            int genreId = resultSet.getInt("genre_id");
            if (!resultSet.wasNull()) {
                Genre genre = new Genre();
                genre.setId(genreId);
                genre.setName(resultSet.getString("genre_name"));
                film.getGenres().add(genre);
            }
            int directorId = resultSet.getInt("director_id");
            if (!resultSet.wasNull()) {
                Director director = new Director();
                director.setId(directorId);
                director.setName(resultSet.getString("director_name"));
                film.getDirectors().add(director);
            }
        }
        return new ArrayList<>(filmMap.values());
    }
}
