package mana.game.shop.repository;

import mana.game.shop.entity.Transaction;
import mana.game.shop.entity.User;
import mana.game.shop.util.DatabaseUtil;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionRepositoryImpl implements TransactionRepository {
    private DataSource dataSource;

    public TransactionRepositoryImpl (DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean saveTransaction(Connection connection, int userId, int gameId, BigDecimal amount) throws SQLException {
        String sql = """
                INSERT INTO transactions 
                (user_id, game_id, amount) 
                VALUES 
                (?, ?, ?)
                """;
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, userId);
        preparedStatement.setInt(2, gameId);
        preparedStatement.setBigDecimal(3, amount);
        int rowsUpdated = preparedStatement.executeUpdate();
        return rowsUpdated > 0;
    }

    @Override
    public boolean topup(int id, BigDecimal balance) {
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
    public boolean deductBalance(Connection connection, User user, BigDecimal amount) throws SQLException {
        String sql = """
                UPDATE users 
                set balance = balance - ?,
                updated_at = CURRENT_TIMESTAMP 
                WHERE id = ? AND balance >= ?
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
            }
        }
        return false;
    }

    @Override
    public List<Transaction> findAllByUserId(int userId) {
        String sql = """
                SELECT * FROM transactions 
                WHERE user_id = ?
                """;
        List<Transaction> transactions = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Transaction transaction = rowHelper(resultSet);
                    transactions.add(transaction);
                }
            }

            return transactions;

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to retrieve the user's transaction history: " + exception.getMessage(), exception);
        }
    }

    @Override
    public boolean existsByUserIdAndGameId(int userId, int gameId) {
        String sql = """
                SELECT EXISTS 
                (SELECT 1 FROM transactions 
                WHERE user_id = ? 
                AND game_id = ?)
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, gameId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBoolean(1);
                }
                return false;
            }

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to verify game ownership: " + exception.getMessage(), exception);
        }
    }

    private Transaction rowHelper(ResultSet resultSet) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(resultSet.getInt("id"));
        transaction.setUserId(resultSet.getInt("user_id"));
        transaction.setGameId(resultSet.getInt("game_id"));
        transaction.setAmount(resultSet.getBigDecimal("amount"));
        transaction.setTransactionDate(DatabaseUtil.getInstant(resultSet, "transaction_date"));
        return transaction;
    }
}
