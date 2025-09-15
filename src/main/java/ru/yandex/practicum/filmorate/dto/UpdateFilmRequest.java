package ru.yandex.practicum.filmorate.dto;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
@Data
public class UpdateFilmRequest {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private Set<Long> likes = new HashSet<>();
    public boolean hasId() {
        return ! (id == null );
    }
    public boolean hasName() {
        return ! (name == null || name.isBlank());
    }
    public boolean hasDescription() {
        return ! (description == null || description.isBlank());
    }
    public boolean hasReleaseDate() {
        return ! (releaseDate == null );
    }
    public boolean hasDuration() {
        return ! (duration == null);
    }
    public boolean hasLikes() {
        return ! (likes == null || likes.isEmpty());
    }

}
