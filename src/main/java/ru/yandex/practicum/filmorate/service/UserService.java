package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.UserMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    public UserDto create(NewUserRequest request) {
        if (userRepository.getByEmail(request.getEmail()).isPresent()) {
            throw new ValidationException("Этот email уже используется");
        }
        User users = UserMapper.mapToUser(request);
        if (users.getName() == null) {
            users.setName(users.getLogin());
        }
        users = userRepository.create(users);
        return UserMapper.mapToUserDto(users);
    }

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

        userRepository.addFriend(userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        if (userRepository.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (userRepository.getById(friendId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }
        userRepository.removeFriend(userId, friendId);
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
}