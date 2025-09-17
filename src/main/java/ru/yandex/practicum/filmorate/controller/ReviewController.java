package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ReviewDto create(@Valid @RequestBody NewReviewRequest request) {
        return reviewService.create(request);
    }

    @PutMapping
    public ReviewDto update(@Valid @RequestBody UpdateReviewRequest request) {
        return reviewService.update(request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") int reviewId) {
        reviewService.delete(reviewId);
    }

    @GetMapping("/{id}")
    public ReviewDto findById(@PathVariable("id") int reviewId) {
        return reviewService.findById(reviewId);
    }

    @GetMapping
    public List<ReviewDto> findReviews(
            @RequestParam(value = "filmId", required = false) Integer filmId,
            @RequestParam(value = "count", required = false) Integer count
    ) {
        return reviewService.findReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") int reviewId, @PathVariable("userId") int userId) {
        reviewService.addLike(reviewId, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable("id") int reviewId, @PathVariable("userId") int userId) {
        reviewService.addDislike(reviewId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable("id") int reviewId, @PathVariable("userId") int userId) {
        reviewService.removeLike(reviewId, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable("id") int reviewId, @PathVariable("userId") int userId) {
        reviewService.removeDislike(reviewId, userId);
    }
}