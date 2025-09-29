package ru.yandex.practicum.filmorate.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FeedRepository;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.FeedMapper;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.mappers.UserMapper;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final FilmRepository filmRepository;

    @Autowired
    public UserService(UserRepository userRepository, FeedRepository feedRepository, FilmRepository filmRepository) {
        this.userRepository = userRepository;
        this.feedRepository = feedRepository;
        this.filmRepository = filmRepository;
    }

    public List<UserDto> getAll() {
        return userRepository.getAll().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto getById(int id) {
        Optional<User> mainUser = userRepository.getById(id);

        if (mainUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id: " + id + " не найден");
        }

        return UserMapper.mapToUserDto(mainUser.get());
    }

    @Transactional
    public UserDto create(NewUserRequest request) {
        if (userRepository.getByEmail(request.getEmail()).isPresent()) {
            throw new ValidationException("Этот email уже используется");
        }
        User users = UserMapper.mapToUser(request);
        if (users.getName() == null || users.getName().isBlank()) {
            users.setName(users.getLogin());
        }
        users = userRepository.create(users);
        return UserMapper.mapToUserDto(users);
    }

    @Transactional
    public UserDto update(UpdateUserRequest request) {
        if (request.getId() == null) {
            throw new NotFoundException("Не указан id");
        }
        Optional<User> mainUser = userRepository.getById(request.getId());
        if (mainUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + request.getId() + " не найден");
        }
        User user = mainUser.get();
        if (request.getEmail() != null && !user.getEmail().equals(request.getEmail())
            && userRepository.getByEmail(request.getEmail()).isPresent()) {
            throw new ValidationException("Этот email уже используется");
        }
        User updatedUser = UserMapper.updateUserFields(user, request);
        updatedUser = userRepository.update(updatedUser);
        return UserMapper.mapToUserDto(updatedUser);
    }

    @Transactional
    public void addFriend(int userId, int friendId) {
        if (userRepository.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (userRepository.getById(friendId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }
        if (userId == friendId) {
            throw new ValidationException("Нельзя добавить пользователя в друзья к самому себе");
        }
        if (getFriends(userId).stream().anyMatch(friend -> friend.getId() == friendId)) {
            throw new ValidationException("Пользователи с id = " + userId + " и id = " + friendId +
                                          " уже являются друзьями");
        }
        if (getFriends(userId).stream().anyMatch(friend -> friend.getId() == friendId)) {
            throw new ValidationException("Пользователи с id = " + userId + " и id = " + friendId +
                                          " уже являются друзьями");
        }
        userRepository.addFriend(userId, friendId);
        feedRepository.create(new Feed(userId, friendId, EventType.FRIEND, Operation.ADD));
    }

    @Transactional
    public void removeFriend(int userId, int friendId) {
        if (userRepository.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (userRepository.getById(friendId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }
        userRepository.removeFriend(userId, friendId);
        feedRepository.create(new Feed(userId, friendId, EventType.FRIEND, Operation.REMOVE));
    }

    public List<UserDto> getFriends(int userId) {
        if (userRepository.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        List<User> friends = userRepository.getFriends(userId);
        return friends.stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getCommonFriends(int firstUserId, int secondUserId) {
        if (userRepository.getById(firstUserId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + firstUserId + " не найден");
        }
        if (userRepository.getById(secondUserId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + secondUserId + " не найден");
        }
        List<User> firstUserFriends = userRepository.getFriends(firstUserId);
        List<User> secondUserFriends = userRepository.getFriends(secondUserId);
        List<User> commonFriends = firstUserFriends.stream()
                .filter(secondUserFriends::contains)
                .collect(Collectors.toList());
        return commonFriends.stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public List<FeedDto> getFeed(int id) {
        Optional<User> maybeUser = userRepository.getById(id);
        if (maybeUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return feedRepository.getByUserId(id).stream()
                .map(FeedMapper::mapToEventDto)
                .toList();
    }

    /**
     * @param userId
     * @return list of filmDto recommended for user
     * 1. Фильмы, которые лайкнул текущий пользователь
     * 2. Берём всех пользователей (кроме текущего)
     * 3. Выбираем юзера с наибольшим числом совпадений
     * 4. Возвращаем рекомендации - фильмы, которые лайкнул похожий, но не лайкнул текущий
     */
    public List<FilmDto> getRecommendations(int userId) {
        userRepository.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        List<Film> userLikedFilms = filmRepository.getLikedFilmsByUser(userId);

        List<User> allUsers = userRepository.getAll().stream()
                .filter(u -> u.getId() != userId)
                .collect(Collectors.toList());

        User mostSimilarUser = null;
        int maxIntersection = 0;

        for (User other : allUsers) {
            List<Film> otherLiked = filmRepository.getLikedFilmsByUser(other.getId());
            int intersection = (int) otherLiked.stream()
                    .filter(userLikedFilms::contains)
                    .count();

            if (intersection > maxIntersection) {
                maxIntersection = intersection;
                mostSimilarUser = other;
            }
        }

        if (mostSimilarUser == null) {
            return List.of();
        }

        List<Film> similarUserLiked = filmRepository.getLikedFilmsByUser(mostSimilarUser.getId());

        List<Film> recommendations = similarUserLiked.stream()
                .filter(film -> !userLikedFilms.contains(film))
                .collect(Collectors.toList());

        return recommendations.stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public void deleteById(int userId) {
        if (userRepository.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }
}