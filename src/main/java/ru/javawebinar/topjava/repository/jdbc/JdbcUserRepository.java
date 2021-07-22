package ru.javawebinar.topjava.repository.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.javawebinar.topjava.model.Role;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.repository.UserRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final SimpleJdbcInsert insertUser;

    private final UserExtractor userExtractor = new UserExtractor();

    @Autowired
    public JdbcUserRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.insertUser = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    @Transactional
    public User save(User user) {
        BeanPropertySqlParameterSource parameterSource = new BeanPropertySqlParameterSource(user);

        if (user.isNew()) {
            Number newKey = insertUser.executeAndReturnKey(parameterSource);
            user.setId(newKey.intValue());
        } else if (namedParameterJdbcTemplate.update("""
                   UPDATE users SET name=:name, email=:email, password=:password, 
                   registered=:registered, enabled=:enabled, calories_per_day=:caloriesPerDay WHERE id=:id
                """, parameterSource) == 0) {
            return null;
        }
        jdbcTemplate.update("DELETE FROM user_roles WHERE user_id=?", user.getId());
        jdbcTemplate.batchUpdate("INSERT INTO user_roles (user_id, role) values (?, ?)",
                user.getRoles(),
                user.getRoles().size(),
                new ParameterizedPreparedStatementSetter<Role>() {
                    @Override
                    public void setValues(PreparedStatement ps, Role role) throws SQLException {
                        ps.setString(1, user.getId().toString());
                        ps.setString(2, role.name());
                    }
                });
        return user;
    }

    @Override
    @Transactional
    public boolean delete(int id) {
        return jdbcTemplate.update("DELETE FROM users WHERE id=?", id) != 0;
    }

    @Override
    public User get(int id) {
        String sql = """
                    select u.id as user_id, u.name as user_name, u.email as user_email, u.password as user_password, 
                    u.registered as user_registered, u.enabled as user_enabled,
                           u.calories_per_day as user_calories, ur.role as user_role
                    from users as u left join user_roles as ur on ur.user_id = u.id  where u.id=? order by u.id desc
                """;
        List<User> users = jdbcTemplate.query(sql, userExtractor, id);
        return DataAccessUtils.singleResult(users);
    }

    @Override
    public User getByEmail(String email) {
        String sql = """
                    select u.id as user_id, u.name as user_name, u.email as user_email, u.password as user_password, 
                    u.registered as user_registered, u.enabled as user_enabled,
                           u.calories_per_day as user_calories, ur.role as user_role
                    from users as u left join user_roles as ur on ur.user_id = u.id where u.email=? order by u.id desc 
                """;
        List<User> users = jdbcTemplate.query(sql, userExtractor, email);
        return DataAccessUtils.singleResult(users);
    }

    @Override
    public List<User> getAll() {
        String sql = """
                    select u.id as user_id, u.name as user_name, u.email as user_email, u.password as user_password, 
                    u.registered as user_registered, u.enabled as user_enabled,
                           u.calories_per_day as user_calories, ur.role as user_role
                    from users as u left join user_roles as ur on ur.user_id = u.id order by u.name, u.email desc
                """;
        return jdbcTemplate.query(sql, userExtractor);
    }

    private class UserExtractor implements ResultSetExtractor<List<User>> {
        @Override
        public List<User> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            Map<Integer, User> data = new LinkedHashMap<>();
            while (resultSet.next()) {
                Integer userId = resultSet.getInt("user_id");
                User user = data.get(userId);
                if (user == null) {
                    user = new User(userId,
                            resultSet.getString("user_name"),
                            resultSet.getString("user_email"),
                            resultSet.getString("user_password"),
                            resultSet.getInt("user_calories"),
                            resultSet.getBoolean("user_enabled"),
                            resultSet.getDate("user_registered"), new ArrayList<>());
                }
                String role = resultSet.getString("user_role");
                if (StringUtils.hasLength(role)) {
                    user.getRoles().add(Role.valueOf(role));
                }
                data.put(userId, user);
            }
            return data.values().stream().toList();
        }
    }
}
