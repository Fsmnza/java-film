package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.*;

import java.util.*;

@Repository
public class UserRepository extends FoundRepository {
    private static final String TABLE_NAME = "users";
    private static final String FIND_ALL_QUERY = "SELECT * FROM " + TABLE_NAME;
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM " + TABLE_NAME + " WHERE user_id = ?";
    private static final String FIND_BY_EMAIL_QUERY = "SELECT * FROM " + TABLE_NAME + " WHERE email = ?";
    private static final String INSERT_QUERY = "INSERT INTO " + TABLE_NAME +
                                               "(email, login, name, birthday)" +
                                               "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE " + TABLE_NAME + " " +
                                               "SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
    private static final String FIND_FRIENDS_QUERY = "SELECT u.user_id, u.email, u.login, u.name, u.birthday " +
                                                     "FROM friendships AS f JOIN " + TABLE_NAME + " AS u ON f.friend_id = u.user_id " +
                                                     "WHERE f.user_id = ?";
    private static final String INSERT_INTO_FRIENDSHIPS_QUERY = "INSERT INTO friendships(user_id, friend_id, status) " +
                                                                "VALUES(?, ?, true)";
    private static final String DELETE_FROM_FRIENDSHIPS_QUERY = "DELETE FROM friendships " +
                                                                "WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_FRIENDSHIP_BETWEEN_USERS = "SELECT id from friendships f where user_id = ? and " +
                                                                "friend_id = ? and f.status = true";
    private static final String DELETE_USER_QUERY = "DELETE FROM " + TABLE_NAME + " WHERE user_id = ?";
    private static final String DELETE_USER_FRIENDSHIPS_QUERY = "DELETE FROM friendships WHERE user_id = ? OR friend_id = ?";
    private static final String DELETE_USER_LIKES_QUERY = "DELETE FROM film_likes WHERE user_id = ?";
    private final RatingRepository ratingRepository;
    private final GenreRepository genreRepository;

    @Autowired
    public UserRepository(JdbcTemplate jdbcTemplate, RowMapper<User> rowMapper, RatingRepository ratingRepository, GenreRepository genreRepository) {
        super(jdbcTemplate, rowMapper);
        this.ratingRepository = ratingRepository;
        this.genreRepository = genreRepository;
    }

    public List<User> getAll() {
        log.debug("Запрос на получение всех строк таблицы users");
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<User> getById(int userId) {
        log.debug("Запрос на получение строки таблицы users с id = {}", userId);
        return findOne(FIND_BY_ID_QUERY, userId);
    }

    public Optional<User> getByEmail(String email) {
        log.debug("Запрос на получение строки таблицы users с email = {}", email);
        return findOne(FIND_BY_EMAIL_QUERY, email);
    }

    public User create(User user) {
        log.debug("Запрос на вставку в таблицу users");
        int id = insert(INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        log.debug("Получен новый id = {}", id);
        user.setId(id);

        log.debug("Добавлена строка в таблицу users с id = {}", id);
        return user;
    }

    public User update(User user) {
        log.debug("Запрос на обновление строки в таблице films с id = {}", user.getId());
        update(UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );

        log.debug("Обновлена строка в таблице users с id = {}", user.getId());
        return user;
    }

    public void addFriend(int userId, int friendId) {
        log.debug("Запрос на вставку строки в таблицу friendships");
        insert(INSERT_INTO_FRIENDSHIPS_QUERY, userId, friendId);
        log.debug("Добавлена строка в таблицу friendships: user_id = {}, friend_id = {}", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        log.debug("Запрос на удаление строки из таблицы friendships");
        update(DELETE_FROM_FRIENDSHIPS_QUERY, userId, friendId);
        log.debug("Удалена строка из таблицы friendships: user_id = {}, friend_id = {}", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        log.debug("Запрос на получение всех друзей пользователя с id = {}", userId);
        return findMany(FIND_FRIENDS_QUERY, userId);
    }

//    public boolean isFriendshipExist(int userId, int friendId) {
//        log.debug("Запрос на получение дружбы между двумя пользователями");
//        return findOne(FIND_FRIENDSHIP_BETWEEN_USERS, userId, friendId).isPresent();
//    }

    public void deleteById(int userId) {
        log.debug("Запрос на удаление пользователя с id = {}", userId);
        update(DELETE_USER_FRIENDSHIPS_QUERY, userId, userId);
        update(DELETE_USER_LIKES_QUERY, userId);
        update(DELETE_USER_QUERY, userId);
        log.debug("Пользователь с id = {} удален", userId);
    }

    public Map<Integer, List<Film>> getAllUsersWhoLikedFilms() {
        String query = "SELECT fl.user_id, f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id " +
                       "FROM film_likes fl " +
                       "JOIN films f ON fl.film_id = f.film_id";
        return jdbcTemplate.query(query, rs -> {
            Map<Integer, List<Film>> userFilmsMap = new HashMap<>();
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                int mpaId = rs.getInt("rating_id");
                Rating mpa = ratingRepository.getRatingById(mpaId);
                if (mpa == null) {
                    mpa = new Rating(0, "Нету рейтинга");
                }
                Set<Genre> genres = genreRepository.getGenresByFilmId(rs.getInt("film_id"));
                if (genres == null) {
                    genres = new HashSet<>();
                }
                Film film = new Film(
                        rs.getInt("film_id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getDate("release_date").toLocalDate(),
                        rs.getInt("duration"),
                        mpa,
                        genres
                );
                userFilmsMap.computeIfAbsent(userId, k -> new ArrayList<>()).add(film);
            }
            return userFilmsMap;
        });
    }
}