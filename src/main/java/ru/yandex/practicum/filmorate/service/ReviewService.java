package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FeedRepository;
import ru.yandex.practicum.filmorate.dal.ReviewRepository;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
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
    private final FilmService filmService;
    private final UserService userService;
    private final FeedRepository feedRepository;

    public ReviewService(ReviewRepository reviewRepository, FilmService filmService, UserService userService,
                         FeedRepository feedRepository) {
        this.reviewRepository = reviewRepository;
        this.filmService = filmService;
        this.userService = userService;
        this.feedRepository = feedRepository;
    }

    public ReviewDto create(NewReviewRequest request) {
        if (filmService.getById(request.getFilmId()) == null) {
            throw new NotFoundException("Фильм не найден: " + request.getFilmId());
        }
        try {
            userService.getById(request.getUserId());
        } catch (NotFoundException e) {
            throw new NotFoundException("Пользователь не найден: " + request.getUserId());
        }
        Review review = ReviewMapper.mapToReview(request);
        Review created = reviewRepository.create(review);
        feedRepository.create(new Feed(created.getUserId(), created.getReviewId(),
                EventType.REVIEW, Operation.ADD));
        return ReviewMapper.mapToReviewDto(created);
    }

    public ReviewDto update(UpdateReviewRequest request) {
        Review review = reviewRepository.getById(request.getReviewId())
                .orElseThrow(() -> new NotFoundException("Отзыв не найден: " + request.getReviewId()));

        if (!request.hasContent() && !request.hasIsPositive()) {
            throw new ValidationException("Не указано ни одно поле для обновления");
        }
        ReviewMapper.updateReviewFields(review, request);
        feedRepository.create(new Feed(review.getUserId(), review.getReviewId(),
                EventType.REVIEW, Operation.UPDATE));
        Review updated = reviewRepository.update(review);
        return ReviewMapper.mapToReviewDto(updated);
    }

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
            userService.getById(userId);
        } catch (NotFoundException e) {
            throw new NotFoundException("Пользователь не найден: " + userId);
        }
    }
}