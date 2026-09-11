package mana.game.shop.entity;

import java.sql.Timestamp;

public class Transaction {
    private int id;
    private int userId;
    private int gameId;
    private double amount;
    private Timestamp transactionDate;

    public Transaction(int userId, int gameId, double amount) {
        this.userId = userId;
        this.gameId = gameId;
        this.amount = amount;
        this.transactionDate = new Timestamp(System.currentTimeMillis());
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

    public void setTransactionDate(Timestamp transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Timestamp getTransactionDate() {
        return transactionDate;
    }
}
