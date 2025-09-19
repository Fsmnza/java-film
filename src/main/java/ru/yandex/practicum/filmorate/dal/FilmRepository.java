package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FoundFilmRepository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
                FROM films f
                LEFT JOIN ratings r ON f.rating_id = r.rating_id
                LEFT JOIN film_genres fg ON f.film_id = fg.film_id
                LEFT JOIN genres g ON fg.genre_id = g.genre_id
                LEFT JOIN film_directors fd ON f.film_id = fd.film_id
                LEFT JOIN directors d ON fd.director_id = d.director_id
                ORDER BY f.film_id
            """;
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
    private static final String INSERT_FILM_QUERY = "INSERT INTO " + TABLE_NAME +
                                                    "(name, description, release_date, duration, rating_id) " +
                                                    "VALUES(?, ?, ?, ?, ?)";
    private static final String INSERT_FILM_GENRE_QUERY = "INSERT INTO film_genres(film_id, genre_id) " +
                                                          "VALUES(?, ?)";
    private static final String UPDATE_QUERY = "UPDATE " + TABLE_NAME + " " +
                                               "SET name = ?, description = ?, release_date = ?, duration = ? WHERE film_id = ?";
    private static final String INSERT_FILM_LIKES_QUERY = "INSERT INTO film_likes(film_id, user_id) " +
                                                          "VALUES(?, ?)";
    private static final String DELETE_FROM_FILM_LIKES_QUERY = "DELETE FROM film_likes " +
                                                               "WHERE film_id = ? AND user_id = ?";
    private static final String GET_POPULAR_QUERY = """
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
            JOIN film_likes AS fl ON f.film_id = fl.film_id
            LEFT JOIN ratings AS r ON f.rating_id = r.rating_id
            LEFT JOIN film_genres AS fg ON f.film_id = fg.film_id
            LEFT JOIN genres AS g ON fg.genre_id = g.genre_id
            GROUP BY film_id, genre_id
            ORDER BY COUNT(fl.user_id) DESC
            LIMIT ?
            """;
    private static final String FIND_BY_DIRECTOR_SORTED_BY_LIKES = """
                SELECT f.film_id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       r.rating_id,
                       r.name AS rating_name,
                       COUNT(fl.user_id) AS likes_count
                FROM films f
                JOIN film_directors fd ON f.film_id = fd.film_id
                LEFT JOIN film_likes fl ON f.film_id = fl.film_id
                LEFT JOIN ratings r ON f.rating_id = r.rating_id
                WHERE fd.director_id = ?
                GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, r.rating_id, r.name
                ORDER BY likes_count DESC
            """;


    private static final String FIND_BY_DIRECTOR_SORTED_BY_YEAR = """
                SELECT f.film_id AS film_id,
                       f.name AS film_name,
                       f.description AS film_description,
                       f.release_date AS film_release_date,
                       f.duration AS film_duration,
                       r.rating_id AS rating_id,
                       r.name AS rating_name,
                       g.genre_id AS genre_id,
                       g.name AS genre_name
                FROM films AS f
                JOIN film_directors fd ON f.film_id = fd.film_id
                LEFT JOIN ratings AS r ON f.rating_id = r.rating_id
                LEFT JOIN film_genres fg ON f.film_id = fg.film_id
                LEFT JOIN genres g ON fg.genre_id = g.genre_id
                WHERE fd.director_id = ?
                ORDER BY f.release_date
            """;

    private static final String GET_FILM_LIKES_QUERY = "SELECT user_id FROM film_likes WHERE film_id = ?";
    private final FoundFilmRepository foundFilmRepository;

    @Autowired
    public FilmRepository(JdbcTemplate jdbcTemplate, RowMapper<Film> rowMapper, FoundFilmRepository foundFilmRepository) {
        super(jdbcTemplate, rowMapper);
        this.foundFilmRepository = foundFilmRepository;
    }

    public List<Film> getAll() {
        log.debug("Запрос на получение всех строк таблицы films");
        return findMany(FIND_ALL_QUERY, foundFilmRepository);
    }

    public Optional<Film> getById(int filmId) {
        log.debug("Запрос на получение строки таблицы films с id = {}", filmId);
        return findOne(FIND_BY_ID_QUERY, foundFilmRepository, filmId);
    }

    public Film create(Film film) {
        log.debug("Запрос на вставку в таблицу films");
        int id = insert(INSERT_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );
        log.debug("Получен новый id = {}", id);
        film.setId(id);

        for (Genre genre : film.getGenres()) {
            insert(INSERT_FILM_GENRE_QUERY, film.getId(), genre.getId());
            log.debug("Добавлена строка в таблицу film_genres: film_id = {}, genre_id = {}",
                    film.getId(), genre.getId());
        }

        log.debug("Добавлена строка в таблицу films с id = {}", id);
        return film;
    }

    public Film update(Film film) {
        log.debug("Запрос на обновление строки в таблице films с id = {}", film.getId());
        update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getId());

        log.debug("Обновлена строка в таблице films с id = {}", film.getId());
        return film;
    }

    public void putLike(int filmId, int userId) {
        log.debug("Запрос на вставку строки в таблицу film_likes");
        insert(INSERT_FILM_LIKES_QUERY, filmId, userId);
        log.debug("Добавлена строка в таблицу film_likes: film_id = {}, user_id = {}", filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        log.debug("Запрос на удаление строки из таблицы film_likes");
        update(DELETE_FROM_FILM_LIKES_QUERY, filmId, userId);
        log.debug("Удалена строка из таблицы film_likes: film_id = {}, user_id = {}", filmId, userId);
    }

    public List<Film> getPopular(int count) {
        log.debug("Запрос на получение первых {} популярных фильмов", count);
        return findMany(GET_POPULAR_QUERY, foundFilmRepository, count);
    }

    public List<Integer> getLikesUserId(int filmId) {
        log.debug("Запрос на получение всех user_id из таблицы film_likes для film_id = {}", filmId);
        return super.findManyInts(GET_FILM_LIKES_QUERY, filmId);
    }

    public List<Film> getFilmsByDirectorSortedByLikes(Integer directorId) {
        log.debug("Запрос на получение фильмов режиссера {} с сортировкой по лайкам", directorId);
        List<Film> films = findMany(FIND_BY_DIRECTOR_SORTED_BY_LIKES, foundFilmRepository, directorId);
        for (Film film : films) {
            Set<User> likes = new HashSet<>();
            List<Integer> userIds = getLikesUserId(film.getId());
            for (Integer userId : userIds) {
                User user = new User();
                user.setId(userId);
                likes.add(user);
            }
            film.setLikes(likes);
        }
        return films;
    }


    public List<Film> getFilmsByDirectorSortedByYear(Integer directorId) {
        log.debug("Запрос на получение фильмов режиссера {} с сортировкой по году", directorId);
        return findMany(FIND_BY_DIRECTOR_SORTED_BY_YEAR, foundFilmRepository, directorId);
    }
}
