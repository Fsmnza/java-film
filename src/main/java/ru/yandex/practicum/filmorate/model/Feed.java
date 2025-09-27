package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.Instant;

@Data
public class Feed {
    private Integer event_id;
    private Integer userId;
    private Integer entityId;
    private EventType eventType;
    private Operation operation;
    private Instant timestamp;

    public Feed() {

    }

    public Feed(int userId, int entityId, EventType eventType, Operation operation) {
        this.userId = userId;
        this.entityId = entityId;
        this.eventType = eventType;
        this.operation = operation;
        timestamp = Instant.now();
    }
}