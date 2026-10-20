package mana.game.shop.repository;

import mana.game.shop.entity.User;
import mana.game.shop.entity.UserRole;
import mana.game.shop.exception.DuplicateDataException;
import mana.game.shop.exception.ResourceNotFoundException;
import mana.game.shop.util.DatabaseUtil;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {
    private DataSource dataSource;

    public UserRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public User save(User user) {
        String sql = """
            INSERT INTO users(username, password, email, role)
            VALUES (?, ?, ?, ?::user_role)
            RETURNING id, balance, is_active, created_at, updated_at
            """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setString(4, user.getUserRole().name());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    user.setId(resultSet.getInt("id"));
                    user.setBalance(resultSet.getBigDecimal("balance"));
                    user.setIs_active(resultSet.getBoolean("is_active"));
                    user.setCreated_at(resultSet.getObject("created_at", OffsetDateTime.class).toInstant());
                    user.setUpdated_at(resultSet.getObject("updated_at", OffsetDateTime.class).toInstant());
                }
            }
            return user;
        } catch (SQLException exception) {
            if ("23505".equals(exception.getSQLState())) {
                throw new DuplicateDataException("Username or email already exists!");
            }

            throw new RuntimeException("Database error while saving user: " + exception.getMessage(), exception);
        }
    }

    @Override
    public Optional<User> update(User user) {
        String sql = """
            UPDATE users
            SET username = ?,
                password = ?,
                email = ?,
                role = ?::user_role,
                balance = ?,
                is_active = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            RETURNING updated_at
            """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setString(4, user.getUserRole().name());
            preparedStatement.setBigDecimal(5, user.getBalance());
            preparedStatement.setBoolean(6, user.isIs_active());
            preparedStatement.setLong(7, user.getId());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    user.setUpdated_at(resultSet.getObject("updated_at", OffsetDateTime.class).toInstant());
                    return Optional.of(user);
                }
            }
            return Optional.empty();

        } catch (SQLException exception) {
            throw new RuntimeException("Database error while saving user: " + exception.getMessage(), exception);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = """
                DELETE FROM users 
                WHERE id = ?
                """;
        try(Connection connection = dataSource.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);

            int changedRow = preparedStatement.executeUpdate();
            return changedRow > 0;
        } catch (SQLException exception) {
            throw new ResourceNotFoundException(exception.getMessage());
        }
    }

    @Override
    public boolean increaseBalance(int id, BigDecimal balance) {
        String sql = """
                UPDATE users 
                set balance = balance + ?, 
                updated_at = CURRENT_TIMESTAMP 
                WHERE id = ?
                """;

        try(Connection connection = dataSource.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setBigDecimal(1, balance);
            preparedStatement.setInt(2, id);

            int rowsUpdated = preparedStatement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    @Override
    public boolean decreaseBalance(Connection connection, User user, BigDecimal amount) {
        String sql = """
                UPDATE users 
                SET balance = balance - ?,
                updated_at = CURRENT_TIMESTAMP 
                WHERE id = ? AND balance >= ?
                RETURNING balance, updated_at
                """;

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setBigDecimal(1, amount);
            preparedStatement.setInt(2, user.getId());
            preparedStatement.setBigDecimal(3, amount);

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.next()) {
                    user.setBalance(resultSet.getBigDecimal("balance"));
                    user.setUpdated_at(resultSet.getObject("updated_at", OffsetDateTime.class).toInstant());
                    return true;
                }
            } catch (SQLException exception) {
                throw new RuntimeException(exception.getMessage());
            }
        } catch (SQLException exception) {
            throw new RuntimeException(exception.getMessage());
        }
        return false;
    }

    @Override
    public Optional<User> bannedUser(boolean is_active, int id) {
        String sql = """
                UPDATE users 
                SET is_active = ?, 
                updated_at = CURRENT_TIMESTAMP 
                WHERE id = ?
                RETURNING updated_at
                """;
        try(Connection connection = dataSource.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setBoolean(1, is_active);
            preparedStatement.setInt(2, id);

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.next()) {
                    User bannedUser = findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found!"));
                    bannedUser.setUpdated_at(resultSet.getObject("updated_at", OffsetDateTime.class).toInstant());
                    return Optional.of(bannedUser);
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = """
                SELECT *
                FROM users 
                WHERE id = ?
                """;
        try(Connection connection = dataSource.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                User user = rowHelper(resultSet);
                return Optional.of(user);
            }
            return Optional.empty();
        } catch (SQLException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Override
    public Optional<User> login(String username) {
        String sql = """
                SELECT *
                FROM users 
                WHERE username = ?
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(rowHelper(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Database error: " + exception.getMessage(), exception);
        }
    }
    @Override
    public List<User> findAll(UserRole role) {
        String sql = "SELECT * FROM users WHERE role = ?::user_role ORDER BY id";
        List<User> users = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, role.name());

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(rowHelper(resultSet));
                }
            }

            return users;

        } catch (SQLException exception) {
            throw new RuntimeException("Database error while fetching users by role: " + exception.getMessage());
        }
    }

    private User rowHelper(ResultSet resultSet) throws SQLException {
        UserRole userRole = UserRole.valueOf(resultSet.getString("role"));
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setUsername(resultSet.getString("username"));
        user.setPassword(resultSet.getString("password"));
        user.setEmail(resultSet.getString("email"));
        user.setUserRole(userRole);
        user.setBalance(resultSet.getBigDecimal("balance"));
        user.setIs_active(resultSet.getBoolean("is_active"));
        user.setCreated_at(DatabaseUtil.getInstant(resultSet, "created_at"));
        user.setUpdated_at(DatabaseUtil.getInstant(resultSet, "updated_at"));
        return user;
    }
}
