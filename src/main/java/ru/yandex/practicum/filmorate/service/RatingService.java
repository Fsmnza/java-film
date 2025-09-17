package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.RatingRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.List;
import java.util.Optional;

@Service
public class RatingService {
    private final RatingRepository ratingRepository;

    @Autowired
    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    public List<Rating> getAll() {
        return ratingRepository.getAll();
    }

    public Rating getById(int id) {
        Optional<Rating> mainRating = ratingRepository.getById(id);
        if (mainRating.isEmpty()) {
            throw new NotFoundException("Рейтинг с id = " + id + " не найден");
        }

        return mainRating.get();
    }
}
