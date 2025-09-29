package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.FoundFilmRepository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class FilmRepository extends FoundRepository<Film> {
    private static final String TABLE_NAME = "films";
    private static final String FIND_ALL_QUERY = """
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
            ORDER BY f.film_id""";

    private static final String FIND_BY_ID_QUERY = """
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
            WHERE f.film_id = ?""";

    private static final String GET_POPULAR_FILM_IDS_QUERY = """
            SELECT f.film_id
            FROM films f
            LEFT JOIN film_likes fl ON f.film_id = fl.film_id
            WHERE 1=1
            %s
            GROUP BY f.film_id
            ORDER BY COUNT(fl.user_id) DESC
            LIMIT ?
            """;

    private static final String GET_FILMS_BY_IDS_QUERY = """
            SELECT f.film_id AS film_id,
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
            FROM films f
            LEFT JOIN ratings r ON f.rating_id = r.rating_id
            LEFT JOIN film_genres fg ON f.film_id = fg.film_id
            LEFT JOIN genres g ON fg.genre_id = g.genre_id
            LEFT JOIN film_directors fd ON f.film_id = fd.film_id
            LEFT JOIN directors d ON fd.director_id = d.director_id
            WHERE f.film_id IN (%s)
            ORDER BY f.film_id
            """;

    private static final String GET_FILMS_BY_DIRECTOR_SORTED_BY_LIKES = """
            SELECT f.film_id AS film_id, f.name AS film_name, f.description AS film_description,
                   f.release_date AS film_release_date, f.duration AS film_duration,
                   r.rating_id AS rating_id, r.name AS rating_name,
                   d.director_id AS director_id, d.name AS director_name,
                   g.genre_id AS genre_id, g.name AS genre_name,
                   COUNT(fl.user_id) AS likes_count
            FROM films f
            JOIN film_directors fd ON f.film_id = fd.film_id
            JOIN directors d ON fd.director_id = d.director_id
            LEFT JOIN ratings r ON f.rating_id = r.rating_id
            LEFT JOIN film_likes fl ON f.film_id = fl.film_id
            LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id
            LEFT JOIN genres AS g ON fg.genre_id = g.genre_id
            WHERE fd.director_id = ?
            GROUP BY f.film_id, r.rating_id, r.name, d.director_id, d.name
            ORDER BY likes_count DESC
            """;

    private static final String GET_FILMS_BY_DIRECTOR_SORTED_BY_YEAR = """
            SELECT f.film_id AS film_id, f.name AS film_name, f.description AS film_description,
                   f.release_date AS film_release_date, f.duration AS film_duration,
                   r.rating_id AS rating_id, r.name AS rating_name,
                   d.director_id AS director_id, d.name AS director_name,
                   g.genre_id AS genre_id, g.name AS genre_name
            FROM films f
            JOIN film_directors fd ON f.film_id = fd.film_id
            JOIN directors d ON fd.director_id = d.director_id
            LEFT JOIN ratings r ON f.rating_id = r.rating_id
            LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id
            LEFT JOIN genres AS g ON fg.genre_id = g.genre_id
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
                g.name AS genre_name,
                d.director_id AS director_id,
                d.name AS director_name
            FROM films AS f
            LEFT JOIN ratings AS r ON f.rating_id = r.rating_id
            LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id
            LEFT JOIN genres AS g ON fg.genre_id = g.genre_id
            LEFT JOIN film_directors AS fd ON f.film_id = fd.film_id
            LEFT JOIN directors AS d ON fd.director_id = d.director_id
            WHERE f.film_id IN (
                SELECT fd2.film_id FROM film_directors fd2
                JOIN directors d2 ON fd2.director_id = d2.director_id
                WHERE LOWER(d2.name) LIKE LOWER(?)
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
                g.name AS genre_name,
                d.director_id AS director_id,
                d.name AS director_name,
                COUNT(fl.user_id) AS likes_count
            FROM films AS f
            LEFT JOIN ratings AS r ON f.rating_id = r.rating_id
            LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id
            LEFT JOIN genres AS g ON fg.genre_id = g.genre_id
            LEFT JOIN film_directors AS fd ON f.film_id = fd.film_id
            LEFT JOIN directors AS d ON fd.director_id = d.director_id
            LEFT JOIN film_likes AS fl ON f.film_id = fl.film_id
            WHERE LOWER(f.name) LIKE LOWER(?)
               OR f.film_id IN (
                    SELECT fd2.film_id FROM film_directors fd2
                    JOIN directors d2 ON fd2.director_id = d2.director_id
                    WHERE LOWER(d2.name) LIKE LOWER(?)
               )
            GROUP BY f.film_id, r.rating_id, r.name, g.genre_id, g.name, d.director_id, d.name
            ORDER BY likes_count DESC
            """;

    private static final String GET_LIKED_FILMS_BY_USER_QUERY = """
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
            JOIN film_likes AS fl ON f.film_id = fl.film_id
            LEFT JOIN ratings AS r ON f.rating_id = r.rating_id
            LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id
            LEFT JOIN genres AS g ON fg.genre_id = g.genre_id
            LEFT JOIN film_directors AS fd ON f.film_id = fd.film_id
            LEFT JOIN directors AS d ON fd.director_id = d.director_id
            WHERE fl.user_id = ?
            ORDER BY f.film_id
            """;

    private static final String GET_COMMON_FILMS_WITH_FRIEND_SORTED_BY_LIKES = """
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
                        JOIN film_likes AS fl ON f.film_id = fl.film_id
                        LEFT JOIN ratings AS r ON f.rating_id = r.rating_id
                        LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id
                        LEFT JOIN genres AS g ON fg.genre_id = g.genre_id
                        LEFT JOIN film_directors AS fd ON f.film_id = fd.film_id
                        LEFT JOIN directors AS d ON fd.director_id = d.director_id
                JOIN film_likes fl1 ON fl1.film_id = f.film_id AND fl1.user_id = ?
                JOIN film_likes fl2 ON fl2.film_id = f.film_id AND fl2.user_id = ?
                LEFT JOIN film_likes fl_all ON fl_all.film_id = f.film_id
                GROUP BY f.film_id
                ORDER BY COUNT(fl_all.user_id) DESC
            """;
    private static final String DELETE_FILM_QUERY = "DELETE FROM " + TABLE_NAME + " WHERE film_id = ?";
    private static final String DELETE_FILM_GENRES_QUERY = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String DELETE_FILM_LIKES_QUERY = "DELETE FROM film_likes WHERE film_id = ?";
    private static final String DELETE_FILM_DIRECTORS_QUERY = "DELETE FROM film_directors WHERE film_id = ?";
    private static final String INSERT_FILM_DIRECTOR_QUERY = "INSERT INTO film_directors(film_id, director_id) VALUES (?, ?)";
    private static final String DELETE_FILM_DIRECTORS_BY_FILM_ID_QUERY = "DELETE FROM film_directors WHERE film_id = ?";
    private static final String INSERT_FILM_QUERY = "INSERT INTO " + TABLE_NAME + "(name, description, release_date," + " duration, rating_id) " + "VALUES(?, ?, ?, ?, ?)";
    private static final String INSERT_FILM_GENRE_QUERY = "INSERT INTO film_genres(film_id, genre_id) " + "VALUES(?, ?)";
    private static final String UPDATE_QUERY =
            "UPDATE films " +
                    "SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? " +
                    "WHERE film_id = ?";
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
        logger.debug("Запрос на вставку в таблицу films");
        int id = insert(INSERT_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );
        logger.debug("Получен новый id = {}", id);
        film.setId(id);

        for (Genre genre : film.getGenres()) {
            insert(INSERT_FILM_GENRE_QUERY, film.getId(), genre.getId());
            logger.debug("Добавлена строка в таблицу film_genres: film_id = {}, genre_id = {}",
                    film.getId(), genre.getId());
        }

        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                insertSimple(INSERT_FILM_DIRECTOR_QUERY, film.getId(), director.getId());
            }
        }
        return film;
    }

    public Film update(Film film) {
        update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        update(DELETE_FILM_GENRES_QUERY, film.getId());
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                insert(INSERT_FILM_GENRE_QUERY, film.getId(), genre.getId());
            }
        }
        update(DELETE_FILM_DIRECTORS_BY_FILM_ID_QUERY, film.getId());
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
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

    public List<Film> getPopular(int count, Integer genreId, Integer year) {
        StringBuilder filter = new StringBuilder();
        List<Object> params = new ArrayList<>();

        if (year != null) {
            filter.append(" AND EXTRACT(YEAR FROM f.release_date) = ?");
            params.add(year);
        }
        if (genreId != null) {
            filter.append(" AND EXISTS (SELECT 1 FROM film_genres fg WHERE fg.film_id = f.film_id AND fg.genre_id = ?)");
            params.add(genreId);
        }

        String filmIdsQuery = String.format(GET_POPULAR_FILM_IDS_QUERY, filter);
        params.add(count);

        List<Integer> filmIds = jdbcTemplate.query(filmIdsQuery, (rs, rowNum) -> rs.getInt("film_id"), params.toArray());

        if (filmIds.isEmpty()) {
            return Collections.emptyList();
        }

        String inSql = filmIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        String filmsQuery = String.format(GET_FILMS_BY_IDS_QUERY, inSql);

        return findMany(filmsQuery, foundFilmRepository);
    }

    public List<Integer> getLikesUserId(int filmId) {
        logger.debug("Запрос на получение всех user_id из таблицы film_likes для film_id = {}", filmId);
        return super.findManyInts(GET_FILM_LIKES_QUERY, filmId);
    }

    public List<Film> getFilmsByDirectorSortedByLikes(int directorId) {
        return findMany(GET_FILMS_BY_DIRECTOR_SORTED_BY_LIKES, foundFilmRepository, directorId);
    }

    public List<Film> getFilmsByDirectorSortedByYear(int directorId) {
        return findMany(GET_FILMS_BY_DIRECTOR_SORTED_BY_YEAR, foundFilmRepository, directorId);
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

    public List<Film> getLikedFilmsByUser(int userId) {
        logger.debug("Запрос на получение всех фильмов, лайкнутых пользователем с id = {}", userId);
        return findMany(GET_LIKED_FILMS_BY_USER_QUERY, foundFilmRepository, userId);
    }

    public List<Film> getCommonFilmsWithFriend(int userId, int friendId) {
        logger.debug("Запрос на получение общих с другом фильмов. Айди пользователя = {}. Айди друга = {}", userId, friendId);
        return findMany(GET_COMMON_FILMS_WITH_FRIEND_SORTED_BY_LIKES, foundFilmRepository, userId, friendId);
    }

    public void deleteById(int filmId) {
        logger.debug("Запрос на удаление фильма с id = {}", filmId);

        update(DELETE_FILM_GENRES_QUERY, filmId);
        update(DELETE_FILM_LIKES_QUERY, filmId);
        update(DELETE_FILM_DIRECTORS_QUERY, filmId);

        update(DELETE_FILM_QUERY, filmId);
        logger.debug("Фильм с id = {} удален", filmId);
    }
}
