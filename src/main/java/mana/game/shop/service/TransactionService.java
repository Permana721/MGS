package mana.game.shop.service;

import mana.game.shop.entity.Transaction;
import mana.game.shop.entity.User;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    boolean buyGame(int userId, int gameId);
    boolean refundTransaction(int transactionId, int userId);
    Transaction findTransaction(int id);
    List<Transaction> findTransactionById(int userId);
    List<Transaction> filteredFindTransactionById(int userId);
    List<Transaction> findTransactionHistory(Instant fromDate, Instant toDate);
}