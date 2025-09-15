package ru.yandex.practicum.filmorate.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class UpdateUserRequest {
    private Integer id;
    private String login;
    private String email;
    private String name;
    private LocalDate birthday;
    private Set<Long> friends = new HashSet<>();

    public boolean hasLogin() {
        return ! (login == null || login.isBlank());
    }

    public boolean hasEmail() {
        return ! (email == null || email.isBlank());
    }

    public boolean hasBirthday() {
        return ! (birthday == null);
    }
    public boolean hasFriends() {
        return ! (friends == null || friends.isEmpty());
    }
    public boolean hasId() {
        return ! (id == null);
    }
}
