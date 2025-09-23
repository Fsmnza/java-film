package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.FoundFilmRepository;
import ru.yandex.practicum.filmorate.model.*;

import java.util.*;

@Repository
@Slf4j
public class FilmRepository extends FoundRepository<Film> {
    private static final String TABLE_NAME = "films";
    private static final String FIND_ALL_QUERY = """
            SELECT f.film_id AS film_id, f.name AS film_name, f.description AS film_description,
                   f.release_date AS film_release_date, f.duration AS film_duration,
                   r.rating_id AS rating_id, r.name AS rating_name,
                   g.genre_id AS genre_id, g.name AS genre_name
            FROM films AS f
            LEFT JOIN ratings AS r ON f.rating_id = r.rating_id
            LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id
            LEFT JOIN genres AS g ON fg.genre_id = g.genre_id
            ORDER BY f.film_id
            """;

    private static final String FIND_BY_ID_QUERY = """
            SELECT f.film_id AS film_id, f.name AS film_name, f.description AS film_description,
                   f.release_date AS film_release_date, f.duration AS film_duration,
                   r.rating_id AS rating_id, r.name AS rating_name,
                   g.genre_id AS genre_id, g.name AS genre_name
            FROM films AS f
            LEFT JOIN ratings AS r ON f.rating_id = r.rating_id
            LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id
            LEFT JOIN genres AS g ON fg.genre_id = g.genre_id
            WHERE f.film_id = ?
            """;

    private static final String GET_POPULAR_QUERY = """
            SELECT f.film_id AS film_id, f.name AS film_name, f.description AS film_description,
                   f.release_date AS film_release_date, f.duration AS film_duration,
                   r.rating_id AS rating_id, r.name AS rating_name,
                   g.genre_id AS genre_id, g.name AS genre_name
            FROM films AS f
            JOIN film_likes AS fl ON f.film_id = fl.film_id
            LEFT JOIN ratings AS r ON f.rating_id = r.rating_id
            LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id
            LEFT JOIN genres AS g ON fg.genre_id = g.genre_id
            GROUP BY film_id, genre_id
            ORDER BY COUNT(fl.user_id) DESC
            LIMIT ?
            """;

    private static final String GET_FILMS_BY_DIRECTOR_SORTED_BY_LIKES = """
        SELECT f.film_id AS film_id, f.name AS film_name, f.description AS film_description,
               f.release_date AS film_release_date, f.duration AS film_duration,
               r.rating_id AS rating_id, r.name AS rating_name,
               d.director_id AS director_id, d.name AS director_name,
               COUNT(fl.user_id) AS likes_count
        FROM films f
        JOIN film_directors fd ON f.film_id = fd.film_id
        JOIN directors d ON fd.director_id = d.director_id
        LEFT JOIN ratings r ON f.rating_id = r.rating_id
        LEFT JOIN film_likes fl ON f.film_id = fl.film_id
        WHERE fd.director_id = ?
        GROUP BY f.film_id, r.rating_id, r.name, d.director_id, d.name
        ORDER BY likes_count DESC
        """;

    private static final String GET_FILMS_BY_DIRECTOR_SORTED_BY_YEAR = """
        SELECT f.film_id AS film_id, f.name AS film_name, f.description AS film_description,
               f.release_date AS film_release_date, f.duration AS film_duration,
               r.rating_id AS rating_id, r.name AS rating_name,
               d.director_id AS director_id, d.name AS director_name
        FROM films f
        JOIN film_directors fd ON f.film_id = fd.film_id
        JOIN directors d ON fd.director_id = d.director_id
        LEFT JOIN ratings r ON f.rating_id = r.rating_id
        WHERE fd.director_id = ?
        ORDER BY f.release_date
        """;

    private static final String SEARCH_BY_TITLE_QUERY = """
        SELECT
            f.film_id AS film_id,
            f.name AS film_name,
            f.description AS film_description,
            f.release_date AS film_release_date,
            f.duration AS film_duration,
            r.rating_id AS rating_id,
            r.name AS rating_name,
            g.genre_id AS genre_id,
            g.name AS genre_name,
            d.director_id AS director_id,
            d.name AS director_name
        FROM films AS f
        LEFT JOIN ratings AS r ON f.rating_id = r.rating_id
        LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id
        LEFT JOIN genres AS g ON fg.genre_id = g.genre_id
        LEFT JOIN film_directors AS fd ON f.film_id = fd.film_id
        LEFT JOIN directors AS d ON fd.director_id = d.director_id
        WHERE LOWER(f.name) LIKE LOWER(?)
        ORDER BY f.film_id
        """;

    private static final String SEARCH_BY_DIRECTOR_QUERY = """
        SELECT
            f.film_id AS film_id,
            f.name AS film_name,
            f.description AS film_description,
            f.release_date AS film_release_date,
            f.duration AS film_duration,
            r.rating_id AS rating_id,
            r.name AS rating_name,
            g.genre_id AS genre_id,
            g.name AS genre_name
        FROM films AS f
        LEFT JOIN ratings AS r ON f.rating_id = r.rating_id
        LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id
        LEFT JOIN genres AS g ON fg.genre_id = g.genre_id
        WHERE f.film_id IN (
            SELECT fd.film_id FROM film_directors fd
            JOIN directors d ON fd.director_id = d.director_id
            WHERE LOWER(d.name) LIKE LOWER(?)
        )
        ORDER BY f.film_id
        """;

    private static final String SEARCH_BY_TITLE_AND_DIRECTOR_QUERY = """
        SELECT
            f.film_id AS film_id,
            f.name AS film_name,
            f.description AS film_description,
            f.release_date AS film_release_date,
            f.duration AS film_duration,
            r.rating_id AS rating_id,
            r.name AS rating_name,
            g.genre_id AS genre_id,
            g.name AS genre_name
        FROM films AS f
        LEFT JOIN ratings AS r ON f.rating_id = r.rating_id
        LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id
        LEFT JOIN genres AS g ON fg.genre_id = g.genre_id
        WHERE LOWER(f.name) LIKE LOWER(?)
           OR f.film_id IN (
                SELECT fd.film_id FROM film_directors fd
                JOIN directors d ON fd.director_id = d.director_id
                WHERE LOWER(d.name) LIKE LOWER(?)
           )
        ORDER BY f.film_id
        """;
    private static final String INSERT_FILM_DIRECTOR_QUERY = "INSERT INTO film_directors(film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_FILM_DIRECTORS_BY_FILM_ID_QUERY = "DELETE FROM film_directors WHERE film_id = ?";
    private static final String INSERT_FILM_QUERY = "INSERT INTO " + TABLE_NAME + "(name, description, release_date," + " duration, rating_id) " + "VALUES(?, ?, ?, ?, ?)";
    private static final String INSERT_FILM_GENRE_QUERY = "INSERT INTO film_genres(film_id, genre_id) " + "VALUES(?, ?)";
    private static final String UPDATE_QUERY = "UPDATE " + TABLE_NAME + " " + "SET name = ?, description = ?, " + "release_date = ?, duration = ? WHERE film_id = ?";
    private static final String INSERT_FILM_LIKES_QUERY = "INSERT INTO film_likes(film_id, user_id) " + "VALUES(?, ?)";
    private static final String DELETE_FROM_FILM_LIKES_QUERY = "DELETE FROM film_likes " + "WHERE film_id = ?" + " AND user_id = ?";
    private static final String GET_FILM_LIKES_QUERY = "SELECT user_id FROM film_likes " + "WHERE film_id = ?";
    private static final Logger logger = LoggerFactory.getLogger(FilmRepository.class);
    private final FoundFilmRepository foundFilmRepository;

    @Autowired
    public FilmRepository(JdbcTemplate jdbcTemplate, FoundFilmRepository foundFilmRepository) {
        super(jdbcTemplate, new FilmRowMapper());
        this.foundFilmRepository = foundFilmRepository;
    }


    public List<Film> getAll() {
        logger.debug("Запрос на получение всех строк таблицы films");
        return findMany(FIND_ALL_QUERY, foundFilmRepository);
    }

    public Optional<Film> getById(int filmId) {
        logger.debug("Запрос на получение строки таблицы films с id = {}", filmId);
        return findOne(FIND_BY_ID_QUERY, foundFilmRepository, filmId);
    }

    public Film create(Film film) {
        int id = insert(INSERT_FILM_QUERY, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId());
        film.setId(id);

        for (Genre genre : film.getGenres()) {
            insert(INSERT_FILM_GENRE_QUERY, film.getId(), genre.getId());
        }

        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                insertSimple(INSERT_FILM_DIRECTOR_QUERY, film.getId(), director.getId());
            }
        }
        return film;
    }

    public Film update(Film film) {
        update(UPDATE_QUERY, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getId());

        update(DELETE_FILM_DIRECTORS_BY_FILM_ID_QUERY, film.getId());
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                insertSimple(INSERT_FILM_DIRECTOR_QUERY, film.getId(), director.getId());
            }
        }

        return film;
    }

    public void putLike(int filmId, int userId) {
        logger.debug("Запрос на вставку строки в таблицу film_likes");
        insert(INSERT_FILM_LIKES_QUERY, filmId, userId);
        logger.debug("Добавлена строка в таблицу film_likes: film_id = {}, user_id = {}", filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        logger.debug("Запрос на удаление строки из таблицы film_likes");
        update(DELETE_FROM_FILM_LIKES_QUERY, filmId, userId);
        logger.debug("Удалена строка из таблицы film_likes: film_id = {}, user_id = {}", filmId, userId);
    }

    public List<Film> getPopular(int count) {
        logger.debug("Запрос на получение первых {} популярных фильмов", count);
        return findMany(GET_POPULAR_QUERY, foundFilmRepository, count);
    }

    public List<Integer> getLikesUserId(int filmId) {
        logger.debug("Запрос на получение всех user_id из таблицы film_likes для film_id = {}", filmId);
        return super.findManyInts(GET_FILM_LIKES_QUERY, filmId);
    }

    public List<Film> getFilmsByDirectorSortedByLikes(int directorId) {
        return findMany(GET_FILMS_BY_DIRECTOR_SORTED_BY_LIKES,  directorId);
    }

    public List<Film> getFilmsByDirectorSortedByYear(int directorId) {
        return findMany(GET_FILMS_BY_DIRECTOR_SORTED_BY_YEAR, directorId);
    }

    public List<Film> searchFilmsByTitle(String query) {
        logger.debug("Поиск фильмов по названию: {}", query);
        String searchPattern = "%" + query + "%";
        return findMany(SEARCH_BY_TITLE_QUERY, foundFilmRepository, searchPattern);
    }

    public List<Film> searchFilmsByDirector(String query) {
        logger.debug("Поиск фильмов по режиссеру: {}", query);
        String searchPattern = "%" + query + "%";
        return findMany(SEARCH_BY_DIRECTOR_QUERY, foundFilmRepository, searchPattern);
    }

    public List<Film> searchFilmsByTitleAndDirector(String query) {
        logger.debug("Поиск фильмов по названию и режиссеру: {}", query);
        String searchPattern = "%" + query + "%";
        return findMany(SEARCH_BY_TITLE_AND_DIRECTOR_QUERY, foundFilmRepository, searchPattern, searchPattern);
    }
}