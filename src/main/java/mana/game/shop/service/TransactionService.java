package mana.game.shop.service;

import mana.game.shop.entity.Transaction;
import mana.game.shop.entity.User;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface TransactionService {
    boolean buyGame(int userId, int gameId) throws SQLException;
    boolean addBalance(int id, BigDecimal balance);
    boolean decreaseBalance(Connection connection, User user, BigDecimal balance) throws SQLException;
    List<Transaction> findTransactionById(int userId);
}
