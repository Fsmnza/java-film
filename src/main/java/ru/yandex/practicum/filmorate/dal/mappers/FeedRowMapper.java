package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FeedRowMapper implements RowMapper<Feed> {
    @Override
    public Feed mapRow(ResultSet rs, int rowNum) throws SQLException {
        Feed feed = new Feed();
        feed.setEvent_id(rs.getInt("event_id"));
        feed.setTimestamp(rs.getTimestamp("timestamp").toInstant());
        feed.setUserId(rs.getInt("user_id"));
        feed.setEntityId(rs.getInt("entity_id"));
        feed.setEventType(EventType.valueOf(rs.getString("event_type")));
        feed.setOperation(Operation.valueOf(rs.getString("operation")));
        return feed;
    }
}