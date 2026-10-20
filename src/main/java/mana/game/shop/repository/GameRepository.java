package mana.game.shop.repository;

import mana.game.shop.entity.Game;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface GameRepository {
    Optional<Game> save(Game game);
    Optional<Game> update(Game game);
    boolean delete(int id);
    boolean addStock(Connection connection, int id, int stock);
    Optional<Game> decreaseStock(Connection connection, int id, int stock);
    Optional<Game> findById(int id);
    List<Game> findByName(String name);
    List<Game> findAll();
}
