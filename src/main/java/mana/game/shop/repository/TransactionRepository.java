package mana.game.shop.repository;

import mana.game.shop.entity.Transaction;
import mana.game.shop.entity.User;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    boolean saveTransaction(Connection connection, int userId, int gameId, BigDecimal amount) throws SQLException;
    boolean deductBalance(Connection connection, User user, BigDecimal balance) throws SQLException;
    boolean topup(int id, BigDecimal balance);
    List<Transaction> findAllByUserId(int UserId);
    List<Transaction> findTransctionsByDate(Instant fromDate, Instant toDate);
    boolean existsByUserIdAndGameId(int userId, int gameId);
}
