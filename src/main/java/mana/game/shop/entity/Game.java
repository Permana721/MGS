package mana.game.shop.entity;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;

public abstract class Game {
    private int id;
    private String title;
    private GameCategory gameCategory;
    private double price;
    private GameType gameType;
    private Integer stock;
    private double size;
    private LocalDate release_date;
    private Timestamp created_at;

    public Game(String title, GameCategory gameCategory, double price, GameType gameType, Integer stock, double size, LocalDate release_date) {
        this.title = title;
        this.gameCategory = gameCategory;
        this.price = price;
        this.gameType = gameType;
        this.stock = stock;
        this.size = size;
        this.release_date = release_date;
        this.created_at = new Timestamp(System.currentTimeMillis());
    }

    public Game() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public GameCategory getGameCategory() {
        return gameCategory;
    }

    public void setGameCategory(GameCategory gameCategory) {
        this.gameCategory = gameCategory;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public GameType getGameType() {
        return gameType;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public LocalDate getRelease_date() {
        return release_date;
    }

    public void setRelease_date(LocalDate release_date) {
        this.release_date = release_date;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

}