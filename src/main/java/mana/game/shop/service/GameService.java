package mana.game.shop.service;

import mana.game.shop.entity.Game;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface GameService {
    void addGame(Game game);
    void updateGame(Game game);
    void deleteGame(int id);
    Game findGame(int id);
    List<Game> findGamebyName(String name);
    void addGameStock(Connection connection, int id, int stock);
    void decreaseGameStock(Connection connection, int id, int stock);
    List<Game> findAllGame();
}
