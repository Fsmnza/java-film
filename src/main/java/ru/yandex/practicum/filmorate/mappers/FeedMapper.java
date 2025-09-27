package ru.yandex.practicum.filmorate.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FeedDto;
import ru.yandex.practicum.filmorate.model.Feed;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FeedMapper {
    public static FeedDto mapToEventDto(Feed feed) {
        FeedDto feedDto = new FeedDto();
        feedDto.setTimestamp(feed.getTimestamp().toEpochMilli());
        feedDto.setUserId(feed.getUserId());
        feedDto.setEventType(feed.getEventType().name());
        feedDto.setOperation(feed.getOperation().name());
        feedDto.setEntityId(feed.getEntityId());
        return feedDto;
    }
}