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
    public Game addGame(Game game) {
        if (game.getTitle().isBlank() || game.getGameType().getDisplayName().isBlank()) {
            throw new BusinessValidationException("Please fill up all columns!");
        }

        return gameRepository.save(game)
                .orElseThrow(() -> new BusinessValidationException("An error occurred: please try again later!"));
    }

    @Override
    public Game updateGame(Game game) {
        if (game.getTitle().isBlank() || game.getTitle() == null) {
            throw new BusinessValidationException("Game title cannot to be empty!");
        }

        return gameRepository.update(game)
                .orElseThrow(() -> new BusinessValidationException("An error occurred: please try again later!"));
    }

    @Override
    public boolean deleteGame(int id) {
        findGame(id);
        return gameRepository.delete(id);
    }

    @Override
    public Game findGame(int id) {
        if (id == 0) {
            throw new BusinessValidationException("The id must be greater than 0!");
        }

        return gameRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Game with ID " + id + " not found!"));
    }

    @Override
    public List<Game> findGamebyName(String title) {
        if (title.isBlank()) {
            throw new BusinessValidationException("Search keyword cannot be empty!");
        }

        return gameRepository.findByName(title);
    }

    @Override
    public Game addGameStock(Connection connection, int id, int stock) {
        checkTypeGame(id);
        findGame(id);
        return gameRepository.addStock(connection, id, stock)
                .orElseThrow(() -> new BusinessValidationException("Failed to add game stock"));
    }

    @Override
    public Game decreaseGameStock(Connection connection, int id, int stock) {
        checkTypeGame(id);
        findGame(id);
        return gameRepository.decreaseStock(connection, id, stock)
                .orElseThrow(() -> new BusinessValidationException("An error occurred."));
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
