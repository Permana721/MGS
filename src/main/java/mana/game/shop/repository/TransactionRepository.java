package mana.game.shop.repository;

import mana.game.shop.entity.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    boolean saveTransaction(Connection connection, int userId, int gameId, double amount) throws SQLException;
    List<Transaction> findAllByUserId(int UserId);
    boolean existsByUserIdAndGameId(int userId, int gameId);
}
