package mana.game.shop.service;

import com.zaxxer.hikari.HikariDataSource;
import mana.game.shop.entity.*;
import mana.game.shop.repository.GameRepository;
import mana.game.shop.repository.GameRepositoryImpl;
import mana.game.shop.repository.UserRepository;
import mana.game.shop.util.DatabaseUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.List;

public class GameServiceTest {
    private HikariDataSource dataSource;
    private GameRepository gameRepository;
    private DigitalGame digitalGame;
    private PhysicalGame physicalGame;
    private GameService gameService;

    @BeforeEach
    void setUp() {
        dataSource = DatabaseUtil.getDataSource();
        gameRepository = new GameRepositoryImpl(dataSource);
        gameService = new GameServiceImpl(gameRepository);
    }

    @Test
    void addGame() {
        physicalGame = new PhysicalGame("Spiderman", GameCategory.ACTION, 700_000, GameType.PHYSICAL, 100, null, Date.valueOf("2024-01-21").toLocalDate());
        digitalGame = new DigitalGame("Cyberpunk", GameCategory.ADVENTURE, 500_000, GameType.DIGITAL, null, 120_000, Date.valueOf("2022-02-25").toLocalDate());

        gameService.addGame(physicalGame);
        gameService.addGame(digitalGame);
    }

    @Test
    void findById() {
        Game game = gameService.findGame(8);
        System.out.print(game.getGameCategory());
    }

    @Test
    void FindAllGame() {
        List<Game> games = gameService.findAllGame();
        games.forEach(game -> System.out.println(game.getId() + ". " + game.getTitle()));
    }

    @Test
    void deleteGame() {
        List<Game> gamesBefore = gameService.findAllGame();
        System.out.println("Total game before: " + gamesBefore.size());
        boolean result = gameService.deleteGame(6);
        System.out.println(result);
        List<Game> games = gameService.findAllGame();
        System.out.println("Total game now: " + games.size());
    }
}
