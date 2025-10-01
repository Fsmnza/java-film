package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewRepository extends FoundRepository<Review> {

    private static final String TABLE_NAME = "reviews";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM " + TABLE_NAME + " WHERE review_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM " + TABLE_NAME + " ORDER BY useful DESC LIMIT ?";
    private static final String FIND_BY_FILM_ID_QUERY = "SELECT * FROM " + TABLE_NAME +
                                                        " WHERE film_id = ? ORDER BY useful DESC LIMIT ?";

    private static final String INSERT_REVIEW_QUERY = "INSERT INTO " + TABLE_NAME +
                                                      " (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_REVIEW_QUERY = "UPDATE " + TABLE_NAME +
                                                      " SET content = ?, is_positive = ? WHERE review_id = ?";
    private static final String DELETE_REVIEW_QUERY = "DELETE FROM " + TABLE_NAME + " WHERE review_id = ?";

    private static final String INSERT_REVIEW_LIKE_QUERY =
            "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, TRUE)";
    private static final String INSERT_REVIEW_DISLIKE_QUERY =
            "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, FALSE)";
    private static final String DELETE_REVIEW_LIKE_QUERY =
            "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = TRUE";
    private static final String DELETE_REVIEW_DISLIKE_QUERY =
            "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = FALSE";

    private static final String CHECK_LIKE_QUERY =
            "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = TRUE";
    private static final String CHECK_DISLIKE_QUERY =
            "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = FALSE";

    private static final String INCREMENT_USEFUL_QUERY = "UPDATE " + TABLE_NAME + " SET useful = useful + 1 WHERE review_id = ?";
    private static final String DECREMENT_USEFUL_QUERY = "UPDATE " + TABLE_NAME + " SET useful = useful - 1 WHERE review_id = ?";

    @Autowired
    public ReviewRepository(JdbcTemplate jdbcTemplate, RowMapper<Review> rowMapper) {
        super(jdbcTemplate, rowMapper);
    }

    public Optional<Review> getById(int reviewId) {
        return findOne(FIND_BY_ID_QUERY, reviewId);
    }

    public List<Review> getAll(int count) {
        return findMany(FIND_ALL_QUERY, count);
    }

    public List<Review> getByFilmId(int filmId, int count) {
        return findMany(FIND_BY_FILM_ID_QUERY, filmId, count);
    }

    public Review create(Review review) {
        int id = insert(INSERT_REVIEW_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getUseful()
        );
        review.setReviewId(id);
        return review;
    }

    public Review update(Review review) {
        update(UPDATE_REVIEW_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );
        return review;
    }

    public void delete(int reviewId) {
        update(DELETE_REVIEW_QUERY, reviewId);
    }

    public void addLike(int reviewId, int userId) {
        Integer exists = jdbcTemplate.queryForObject(CHECK_LIKE_QUERY, Integer.class, reviewId, userId);
        if (exists != null && exists > 0) return; // лайк уже есть

        removeDislike(reviewId, userId);

        insert(INSERT_REVIEW_LIKE_QUERY, reviewId, userId);
        update(INCREMENT_USEFUL_QUERY, reviewId);
    }

    public void addDislike(int reviewId, int userId) {
        Integer exists = jdbcTemplate.queryForObject(CHECK_DISLIKE_QUERY, Integer.class, reviewId, userId);
        if (exists != null && exists > 0) return; // дизлайк уже есть

        removeLike(reviewId, userId);

        insert(INSERT_REVIEW_DISLIKE_QUERY, reviewId, userId);
        update(DECREMENT_USEFUL_QUERY, reviewId);
    }

    public void removeLike(int reviewId, int userId) {
        Integer exists = jdbcTemplate.queryForObject(CHECK_LIKE_QUERY, Integer.class, reviewId, userId);
        if (exists == null || exists == 0) return; // лайк не существует

        update(DELETE_REVIEW_LIKE_QUERY, reviewId, userId);
        update(DECREMENT_USEFUL_QUERY, reviewId);
    }

    public void removeDislike(int reviewId, int userId) {
        Integer exists = jdbcTemplate.queryForObject(CHECK_DISLIKE_QUERY, Integer.class, reviewId, userId);
        if (exists == null || exists == 0) return; // дизлайк не существует

        update(DELETE_REVIEW_DISLIKE_QUERY, reviewId, userId);
        update(INCREMENT_USEFUL_QUERY, reviewId);
    }
}