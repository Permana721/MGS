package mana.game.shop.service;

import mana.game.shop.entity.Transaction;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransactionService {
    Transaction buyGame(int userId, int gameId, String couponCode);
    boolean refundTransaction(int transactionId, int userId);
    Transaction findTransaction(int id);
    List<Transaction> findTransactionById(int userId);
    List<Transaction> filteredFindTransactionById(int userId);
    List<Transaction> findTransactionHistory(Instant fromDate, Instant toDate);
}