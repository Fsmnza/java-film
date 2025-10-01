package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class GenreRepository extends FoundRepository<Genre> {
    private static final String TABLE_NAME_GENRES = "genres";
    private static final String FIND_ALL_QUERY = "SELECT * FROM " + TABLE_NAME_GENRES;
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM " + TABLE_NAME_GENRES + " WHERE genre_id = ?";
//    protected static final Logger logger = LoggerFactory.getLogger(GenreRepository.class);

    @Autowired
    public GenreRepository(JdbcTemplate jdbcTemplate, RowMapper<Genre> rowMapper) {
        super(jdbcTemplate, rowMapper);
    }

    public List<Genre> getAll() {
        log.debug("Запрос на получение всех строк таблицы genres");
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Genre> getById(int genreId) {
        log.debug("Запрос на получение строки таблицы genres с id = {}", genreId);
        return findOne(FIND_BY_ID_QUERY, genreId);
    }
    public List<Genre> getByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        String placeholders = String.join(",", ids.stream().map(id -> "?").toList());
        String newGet = "SELECT genre_id, name FROM genres WHERE genre_id IN (" + placeholders + ")";
        return findMany(newGet, ids.toArray());
    }
    public Set<Genre> getGenresByFilmId(int filmId) {
        String query = """
        SELECT g.genre_id, g.name 
        FROM film_genres fg 
        JOIN genres g ON fg.genre_id = g.genre_id 
        WHERE fg.film_id = ?
    """;
        List<Genre> genreList = jdbcTemplate.query(query, (rs, rowNum) ->
                new Genre(rs.getInt("genre_id"), rs.getString("name")), filmId);
        return new HashSet<>(genreList);
    }

}

