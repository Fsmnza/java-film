package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class FeedRepository extends FoundRepository<Feed> {

    private static final String INSERT_QUERY = """
            INSERT INTO feed(user_id, timestamp, entity_id, event_type, operation)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String GET_FEED_BY_USER_ID = """
            SELECT event_id,
                   timestamp,
                   user_id,
                   entity_id,
                   event_type,
                   operation
            FROM feed
            WHERE user_id = ?
            ORDER BY timestamp ASC
            """;

    @Autowired
    public FeedRepository(JdbcTemplate jdbcTemplate, RowMapper<Feed> rowMapper) {
        super(jdbcTemplate, rowMapper);
    }

    public void create(Feed feed) {
        newInsert(INSERT_QUERY,
                feed.getUserId(),
                Timestamp.from(feed.getTimestamp()),
                feed.getEntityId(),
                feed.getEventType().name(),
                feed.getOperation().name()
        );
    }

    public List<Feed> getByUserId(int userId) {
        return findMany(GET_FEED_BY_USER_ID, userId);
    }
}
