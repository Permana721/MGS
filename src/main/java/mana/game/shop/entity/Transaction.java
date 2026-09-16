package mana.game.shop.entity;

import java.sql.Timestamp;
import java.time.Instant;

public class Transaction {
    private int id;
    private int userId;
    private int gameId;
    private double amount;
    private Instant transactionDate;

    public Transaction(int userId, int gameId, double amount) {
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setTransactionDate(Instant transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Instant getTransactionDate() {
        return transactionDate;
    }
}
