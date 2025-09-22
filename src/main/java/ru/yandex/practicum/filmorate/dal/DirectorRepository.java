package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

@Repository
public class DirectorRepository extends FoundRepository<Director> {

    private static final String FIND_ALL_QUERY = "SELECT director_id, name  FROM directors";
    private static final String FIND_BY_ID_QUERY = "SELECT director_id, name  FROM directors WHERE director_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO directors (name) VALUES (?)";
    private static final String UPDATE_QUERY = "UPDATE directors SET name = ? WHERE director_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM directors WHERE director_id = ?";


    public DirectorRepository(JdbcTemplate jdbcTemplate,
                              RowMapper<Director> directorRowMapper) {
        super(jdbcTemplate, directorRowMapper);
    }

    public List<Director> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Director> findById(int id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public Director create(Director director) {
        int id = insert(INSERT_QUERY, director.getName());
        director.setId(id);
        return director;
    }

    public Director update(Director director) {
        update(UPDATE_QUERY, director.getName(), director.getId());
        return director;
    }

    public void delete(int id) {
        update(DELETE_QUERY, id);
    }

}
