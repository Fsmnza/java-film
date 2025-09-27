package ru.yandex.practicum.filmorate.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FeedRepository;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.ReviewRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.ReviewMapper;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final FeedRepository feedRepository;

    public ReviewService(ReviewRepository reviewRepository, FilmRepository filmRepository, UserRepository userRepository, FeedRepository feedRepository) {
        this.reviewRepository = reviewRepository;
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
        this.feedRepository = feedRepository;
    }

    @Transactional
    public Review create(Review request) {
        userRepository.getById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + request.getUserId() + " не найден"));
        filmRepository.getById(request.getFilmId())
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + request.getFilmId() + " не найден"));
        Review newReview = reviewRepository.create(request);
        feedRepository.create(new Feed(newReview.getUserId(), newReview.getReviewId(),
                EventType.REVIEW, Operation.ADD));
        return newReview;
    }

    @Transactional
    public Review update(UpdateReviewRequest request) {
        if (request.getReviewId() == null) {
            throw new NotFoundException("Отзыв с id = null не найден");
        }
        Review review = reviewRepository.getById(request.getReviewId())
                .orElseThrow(() -> new NotFoundException("Отзыв с id = " + request.getReviewId() + " не найден"));
        ReviewMapper.updateReviewFields(review, request);
        feedRepository.create(new Feed(review.getUserId(), review.getReviewId(),
                EventType.REVIEW, Operation.UPDATE));
        return reviewRepository.update(review);
    }

    @Transactional
    public void delete(int reviewId) {
        Optional<Review> newReview = reviewRepository.getById(reviewId);
        if (newReview.isEmpty()) {
            throw new NotFoundException("Отзыв с id = " + reviewId + " не найден");
        }

        Review review = newReview.get();
        feedRepository.create(new Feed(review.getUserId(), review.getReviewId(),
                EventType.REVIEW, Operation.REMOVE));
        reviewRepository.delete(reviewId);
    }

    public ReviewDto findById(int reviewId) {
        Review review = reviewRepository.getById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден: " + reviewId));
        return ReviewMapper.mapToReviewDto(review);
    }

    public List<ReviewDto> findReviews(Integer filmId, Integer count) {
        int limit = (count == null) ? 10 : count;
        List<Review> reviews = (filmId != null)
                ? reviewRepository.getByFilmId(filmId, limit)
                : reviewRepository.getAll(limit);

        return reviews.stream()
                .map(ReviewMapper::mapToReviewDto)
                .collect(Collectors.toList());
    }

    public void addLike(int reviewId, int userId) {
        checkExistence(reviewId, userId);
        reviewRepository.addLike(reviewId, userId);
    }

    public void addDislike(int reviewId, int userId) {
        checkExistence(reviewId, userId);
        reviewRepository.addDislike(reviewId, userId);
    }

    public void removeLike(int reviewId, int userId) {
        checkExistence(reviewId, userId);
        reviewRepository.removeLike(reviewId, userId);
    }

    public void removeDislike(int reviewId, int userId) {
        checkExistence(reviewId, userId);
        reviewRepository.removeDislike(reviewId, userId);
    }

    private void checkExistence(int reviewId, int userId) {
        if (reviewRepository.getById(reviewId).isEmpty()) {
            throw new NotFoundException("Отзыв не найден: " + reviewId);
        }
        try {
            userRepository.getById(userId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
    }
}