package mana.game.shop.service;

import mana.game.shop.entity.Game;
import mana.game.shop.entity.GameType;
import mana.game.shop.repository.GameRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class GameServiceImpl implements GameService {
    private final GameRepository gameRepository;

    public GameServiceImpl(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    public Game addGame(Game game) {
        if (Objects.nonNull(game)) {
            return gameRepository.save(game);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public Game updateGame(Game game) {
        if (Objects.nonNull(game)) {
            return gameRepository.update(game);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public boolean deleteGame(int id) {
        if (id >= 0) {
            return gameRepository.delete(id);
        } else {
            throw new RuntimeException("Please input a valid Id!");
        }
    }

    @Override
    public Game findGame(int id) {
        return gameRepository.findById(id).orElseThrow(() -> new RuntimeException("Game with ID " + id + " not found!"));
    }

    @Override
    public boolean decreaseGameStock(Connection connection, int id, int stock) throws SQLException {
        checkTypeGame(id);
        findGame(id);
        return gameRepository.decreaseStock(connection, id, stock);
    }

    @Override
    public List<Game> findAllGame() {
        return gameRepository.findAll();
    }

    private void checkTypeGame(int id) {
        Game game = findGame(id);
        if (game.getGameType() != GameType.PHYSICAL) {
            throw new RuntimeException("Only Physical Game can decrease the stock!");
        }
    }
}
