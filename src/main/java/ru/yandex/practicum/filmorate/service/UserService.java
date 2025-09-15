package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.mappers.UserMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto addUser(NewUserRequest newUser) {
        User user = new User();
        user.setEmail(newUser.getEmail());
        user.setLogin(newUser.getLogin());
        user.setName(newUser.getName());
        user.setBirthday(newUser.getBirthday());
        User saved = userRepository.save(user);
        return mapToDto(saved);
    }

    public UserDto updateUser(UpdateUserRequest updateUser) {
        User user = userRepository.findById(updateUser.getId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (updateUser.getEmail() != null) user.setEmail(updateUser.getEmail());
        if (updateUser.getLogin() != null) user.setLogin(updateUser.getLogin());
        if (updateUser.getName() != null) user.setName(updateUser.getName());
        if (updateUser.getBirthday() != null) user.setBirthday(updateUser.getBirthday());

        User updated = userRepository.update(user);
        return mapToDto(updated);
    }

    public void addFriend(int userId, int friendId) {
        userRepository.addFriend(userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        userRepository.removeFriend(userId, friendId);
    }

    public List<UserDto> getFriends(int userId) {
        return userRepository.getFriendsList(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getMutualFriends(int userId, int otherId) {
        return userRepository.getMutualFriends(userId, otherId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private UserDto mapToDto(User user) {
        UserDto dto = UserMapper.mapToUserDto(user);
        dto.setFriends(user.getFriends());
        return dto;
    }
}
