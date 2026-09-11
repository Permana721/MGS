package mana.game.shop.service;

import mana.game.shop.entity.Transaction;

import java.sql.SQLException;
import java.util.List;

public interface TransactionService {
    boolean buyGame(int userId, int gameId) throws SQLException;
    List<Transaction> findTransactionById(int id);
}
