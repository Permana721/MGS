package mana.game.shop.repository;

import mana.game.shop.entity.Game;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface GameRepository {
    Game save(Game game);
    Game update(Game game);
    boolean delete(int id);
    boolean decreaseStock(Connection connection, int id, int stock) throws SQLException;
    Optional<Game> findById(int id);
    List<Game> findAll();
}
