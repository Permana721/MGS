package mana.game.shop.entity;

import java.math.BigDecimal;
import java.time.Instant;

public class Transaction {
    private int id;
    private int userId;
    private int gameId;
    private BigDecimal amount;
    private TransactionStatus status;
    private Instant transactionDate;
    private Instant updated_at;

    public Transaction(int userId, int gameId, BigDecimal amount) {
        this.userId = userId;
        this.gameId = gameId;
        this.amount = amount;
        this.transactionDate = Instant.ofEpochMilli(System.currentTimeMillis());
    }

    public Transaction() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public void setTransactionDate(Instant transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Instant getTransactionDate() {
        return transactionDate;
    }

    public Instant getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Instant updated_at) {
        this.updated_at = updated_at;
    }
}
