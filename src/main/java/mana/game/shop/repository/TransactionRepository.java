package mana.game.shop.repository;

import mana.game.shop.entity.Transaction;
import mana.game.shop.entity.User;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    boolean saveTransaction(Connection connection, int userId, int gameId, BigDecimal amount) throws SQLException;
    boolean deductBalance(Connection connection, User user, BigDecimal balance) throws SQLException;
    boolean topup(int id, BigDecimal balance);
    List<Transaction> findAllByUserId(int UserId);
    boolean existsByUserIdAndGameId(int userId, int gameId);
}
