package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
public class FoundFilmRepository implements ResultSetExtractor {

    @Override
    public List<Film> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
        final Map<Integer, Film> filmMap = new LinkedHashMap<>();

        while (resultSet.next()) {
            Integer filmId = resultSet.getInt("film_id");

            Film film = filmMap.get(filmId);
            if (film == null) {
                film = new Film();
                film.setId(resultSet.getInt("film_id"));
                film.setName(resultSet.getString("film_name"));
                film.setDescription(resultSet.getString("film_description"));
                film.setReleaseDate(resultSet.getDate("film_release_date").toLocalDate());
                film.setDuration(resultSet.getInt("film_duration"));
                Rating mpaRating = new Rating();
                mpaRating.setId(resultSet.getInt("rating_id"));
                mpaRating.setName(resultSet.getString("rating_name"));
                film.setMpa(mpaRating);
                film.setGenres(new ArrayList<>());
                filmMap.put(filmId, film);
            }
            Integer genreId = resultSet.getInt("genre_id");
            if (!resultSet.wasNull()) {
                Genre genre = new Genre();
                genre.setId(genreId);
                genre.setName(resultSet.getString("genre_name"));
                film.getGenres().add(genre);
            }
        }

        return new ArrayList<>(filmMap.values());
    }
}