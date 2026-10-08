package mana.game.shop.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

public class DigitalGame extends Game {
    public DigitalGame(String title, GameCategory gameCategory, BigDecimal price, GameType gameType, Integer stock, Integer size, LocalDate release_date) {
        super(title, gameCategory, price, gameType, stock, size, release_date);
    }

    public DigitalGame() {
        super();
    }
}
