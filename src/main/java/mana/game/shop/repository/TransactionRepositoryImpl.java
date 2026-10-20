package mana.game.shop.repository;

import mana.game.shop.entity.Transaction;
import mana.game.shop.entity.TransactionStatus;
import mana.game.shop.util.DatabaseUtil;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionRepositoryImpl implements TransactionRepository {
    private final DataSource dataSource;

    public TransactionRepositoryImpl (DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean saveTransaction(Connection connection, int userId, int gameId, BigDecimal amount) {
        String sql = """
                INSERT INTO transactions\s
                (user_id, game_id, amount)\s
                VALUES\s
                (?, ?, ?)
               \s""";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, gameId);
            preparedStatement.setBigDecimal(3, amount);
            int rowsUpdated = preparedStatement.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Override
    public Optional<Transaction> update(Connection connection, int id, TransactionStatus status) {
        String sql = """
                UPDATE transactions
                SET status = ?::transaction_status,
                updated_at = CURRENT_TIMESTAMP
                WHERE id = ?\s
                RETURNING status, updated_at
               \s""";

        try(PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, status.name());
            preparedStatement.setInt(2, id);

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.next()) {
                    return Optional.of(rowHelper(resultSet));
                }
            } catch (SQLException exception) {
                throw new RuntimeException(exception.getMessage());
            }
            return Optional.empty();
        } catch (SQLException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Override
    public List<Transaction> findAllByUserId(int userId) {
        String sql = """
                SELECT * FROM transactions\s
                WHERE user_id = ?
               \s""";
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
                SELECT EXISTS\s
                (SELECT 1 FROM transactions\s
                WHERE user_id = ?\s
                AND game_id = ?)
                FOR UPDATE
               \s""";

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

    @Override
    public List<Transaction> findTransactionsByDate(Instant fromDate, Instant toDate) {
        String sql = """
                SELECT * FROM transactions
                WHERE created_at\s
                BETWEEN ? AND ?\s
                ORDER BY created_at DESC
               \s""";

        List<Transaction> transactions = new ArrayList<>();

        try(Connection connection = dataSource.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, fromDate.atOffset(ZoneOffset.UTC));
            preparedStatement.setObject(2, toDate.atOffset(ZoneOffset.UTC));

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                while (resultSet.next()) {
                    transactions.add(rowHelper(resultSet));
                }
            } catch (SQLException exception) {
                throw new RuntimeException(exception.getMessage());
            }
            return transactions;
        } catch (SQLException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    @Override
    public Optional<Transaction> findTransaction(int id) {
        String sql = """
                SELECT * FROM transactions
                WHERE id = ?
                """;

        try(Connection connection = dataSource.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);

            try(ResultSet resultSet = preparedStatement.executeQuery()){
                if (resultSet.next()) {
                    return Optional.of(rowHelper(resultSet));
                }
            } catch (SQLException exception) {
                throw new RuntimeException(exception.getMessage());
            }
            return Optional.empty();
        } catch (SQLException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }

    private Transaction rowHelper(ResultSet resultSet) throws SQLException {
        TransactionStatus transactionStatus = TransactionStatus.valueOf(resultSet.getString("status"));
        Transaction transaction = new Transaction();
        transaction.setId(resultSet.getInt("id"));
        transaction.setUserId(resultSet.getInt("user_id"));
        transaction.setGameId(resultSet.getInt("game_id"));
        transaction.setAmount(resultSet.getBigDecimal("amount"));
        transaction.setStatus(transactionStatus);
        transaction.setTransactionDate(DatabaseUtil.getInstant(resultSet, "created_at"));
        transaction.setUpdated_at(DatabaseUtil.getInstant(resultSet, "updated_at"));
        return transaction;
    }
}
