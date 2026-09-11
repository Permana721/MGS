package mana.game.shop.entity;

import java.time.LocalDate;
import java.util.Date;

public class DigitalGame extends Game {
    public DigitalGame(String title, GameCategory gameCategory, double price, GameType gameType, Integer stock, double size, LocalDate release_date) {
        super(title, gameCategory, price, gameType, stock, size, release_date);
    }

    public DigitalGame() {
        super();
    }
}
