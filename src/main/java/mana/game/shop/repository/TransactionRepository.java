package mana.game.shop.repository;

import mana.game.shop.entity.Transaction;
import mana.game.shop.entity.TransactionStatus;
import mana.game.shop.entity.User;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    boolean saveTransaction(Connection connection, int userId, int gameId, BigDecimal amount);
    Optional<Transaction> update(Connection connection, int id, TransactionStatus status);
    List<Transaction> findAllByUserId(int UserId);
    List<Transaction> filteredFindAllByUserId(int userId);
    List<Transaction> findTransactionsByDate(Instant fromDate, Instant toDate);
    Optional<Transaction> findTransaction(int id);
    boolean existsByUserIdAndGameId(int userId, int gameId);
}
