package ru.yandex.practicum.filmorate.dto;

import lombok.Data;


@Data
public class FeedDto {
    private Long timestamp;
    private Integer userId;
    private String eventType;
    private String operation;
    private Integer eventId;
    private Integer entityId;
}
