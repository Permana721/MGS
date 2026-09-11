package mana.game.shop.repository;

import mana.game.shop.entity.Transaction;
import mana.game.shop.entity.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionRepositoryImpl implements TransactionRepository {
    private DataSource dataSource;

    public TransactionRepositoryImpl (DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean saveTransaction(Connection connection, int userId, int gameId, double amount) throws SQLException {
        String sql = "INSERT INTO transactions (user_id, game_id, amount) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setInt(1, userId);
        preparedStatement.setInt(2, gameId);
        preparedStatement.setDouble(3, amount);
        int rowsUpdated = preparedStatement.executeUpdate();
        return rowsUpdated > 0;
    }

    @Override
    public List<Transaction> findAllByUserId(int userId) {
        String sql = "SELECT * FROM transactions WHERE user_id = ?";
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
            throw new RuntimeException("Gagal mengambil riwayat transaksi user: " + exception.getMessage(), exception);
        }
    }

    @Override
    public boolean existsByUserIdAndGameId(int userId, int gameId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM transactions WHERE user_id = ? AND game_id = ?)";

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
            throw new RuntimeException("Gagal memeriksa kepemilikan game: " + exception.getMessage(), exception);
        }
    }

    private Transaction rowHelper(ResultSet resultSet) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(resultSet.getInt("id"));
        transaction.setUserId(resultSet.getInt("user_id"));
        transaction.setGameId(resultSet.getInt("game_id"));
        transaction.setAmount(resultSet.getDouble("amount"));
        transaction.setTransactionDate(resultSet.getTimestamp("transaction_date"));
        return transaction;
    }
}
