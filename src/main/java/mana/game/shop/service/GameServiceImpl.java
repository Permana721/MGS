package mana.game.shop.service;

import mana.game.shop.entity.Game;
import mana.game.shop.entity.GameType;
import mana.game.shop.exception.BusinessException;
import mana.game.shop.exception.BusinessValidationException;
import mana.game.shop.exception.ResourceNotFoundException;
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
    public void addGame(Game game) {
        if (game.getTitle().isBlank() || game.getGameType().getDisplayName().isBlank()) {
            throw new BusinessValidationException("Please fill up all columns!");
        }
        gameRepository.save(game);
    }

    @Override
    public void updateGame(Game game) {
        if (game.getTitle().isBlank() || game.getTitle() == null) {
            throw new BusinessValidationException("Game title cannot to be empty!");
        }

        gameRepository.update(game);
    }

    @Override
    public void deleteGame(int id) {
        findGame(id);
        gameRepository.delete(id);
    }

    @Override
    public Game findGame(int id) {
        if (id <= 0) {
            throw new BusinessValidationException("The id must be greater than 0!");
        }

        return gameRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Game with ID " + id + " not found!"));
    }

    @Override
    public List<Game> findGamebyName(String title) {
        if (title.isBlank()) {
            throw new BusinessValidationException("Search keyword cannot be empty!");
        }

        return gameRepository.findByName(title);
    }

    @Override
    public void addGameStock(Connection connection, int id, int stock) {
        checkTypeGame(id);
        findGame(id);
        gameRepository.addStock(connection, id, stock);
    }

    @Override
    public void decreaseGameStock(Connection connection, int id, int stock) {
        checkTypeGame(id);
        findGame(id);
        gameRepository.decreaseStock(connection, id, stock);
    }

    @Override
    public List<Game> findAllGame() {
        return gameRepository.findAll();
    }

    private void checkTypeGame(int id) {
        Game game = findGame(id);
        if (game.getGameType() != GameType.PHYSICAL) {
            throw new BusinessValidationException("Only Physical Game can decrease the stock!");
        }
    }
}
