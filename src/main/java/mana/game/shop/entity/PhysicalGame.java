package mana.game.shop.entity;

import java.time.LocalDate;
import java.util.Date;

public class PhysicalGame extends Game {
    public PhysicalGame(String title, GameCategory gameCategory, double price, GameType gameType, Integer stock, Double size, LocalDate release_date) {
        super(title, gameCategory, price, gameType, stock, size, release_date);
    }

    public PhysicalGame() {
        super();
    }
}
