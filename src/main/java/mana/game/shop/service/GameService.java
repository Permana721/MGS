package mana.game.shop.service;

import mana.game.shop.entity.Game;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface GameService {
    Game addGame(Game game);
    Game updateGame(Game game);
    boolean deleteGame(int id);
    Game findGame(int id);
    boolean decreaseGameStock(Connection connection, int id, int stock) throws SQLException;
    List<Game> findAllGame();
}
