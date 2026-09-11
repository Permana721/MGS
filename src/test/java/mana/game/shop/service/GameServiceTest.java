package mana.game.shop.service;

import com.zaxxer.hikari.HikariDataSource;
import mana.game.shop.entity.*;
import mana.game.shop.repository.GameRepository;
import mana.game.shop.repository.GameRepositoryImpl;
import mana.game.shop.repository.UserRepository;
import mana.game.shop.util.DatabaseUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.mockito.Mockito;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

public class GameServiceTest {
    private HikariDataSource dataSource;
    private GameRepository gameRepository;
    private DigitalGame digitalGame;
    private PhysicalGame physicalGame;
    private GameService gameService;

    private DigitalGame createDigitalGameMock(int id, String title, GameCategory gameCategory, double price, GameType gameType, double size) {
        DigitalGame digitalGame = new DigitalGame();
        digitalGame.setId(id);
        digitalGame.setTitle(title);
        digitalGame.setGameCategory(gameCategory);
        digitalGame.setPrice(price);
        digitalGame.setGameType(gameType);
        digitalGame.setSize(size);
        return digitalGame;
    }

    @BeforeEach
    void setUp() {
        gameRepository = Mockito.mock(GameRepository.class);
        gameService = new GameServiceImpl(gameRepository);
    }

    @Test
    @EnabledOnJre(JRE.JAVA_21)
    @DisplayName("Add Game")
    void addGame() {
        physicalGame = new PhysicalGame("Spiderman", GameCategory.ACTION, 700_000, GameType.PHYSICAL, 100, null, Date.valueOf("2024-01-21").toLocalDate());
        digitalGame = new DigitalGame("Cyberpunk", GameCategory.ADVENTURE, 500_000, GameType.DIGITAL, null, 120_000, Date.valueOf("2022-02-25").toLocalDate());

        Mockito.when(gameRepository.save(physicalGame))
                .thenReturn(physicalGame);

        Mockito.when(gameRepository.save(digitalGame))
                .thenReturn(digitalGame);

        Game addedPhysicalGame = gameService.addGame(physicalGame);
        Game addedDigitalGame = gameService.addGame(digitalGame);

        Assertions.assertEquals(physicalGame.getTitle(), addedPhysicalGame.getTitle());
        Assertions.assertEquals(digitalGame.getTitle(), addedDigitalGame.getTitle());
        Mockito.verify(gameRepository).save(physicalGame);
        Mockito.verify(gameRepository).save(digitalGame);
    }

    @Test
    @EnabledOnJre(JRE.JAVA_21)
    @DisplayName("Find By Id")
    void findById() {
        Game mockGame = createDigitalGameMock(1, "Cyberpunk", GameCategory.ADVENTURE, 500_000, GameType.DIGITAL, 120_000);

        Mockito.when(gameRepository.findById(mockGame.getId()))
                .thenReturn(Optional.of(mockGame));

        Game searchGame = gameService.findGame(mockGame.getId());

        Assertions.assertEquals(searchGame.getTitle(), mockGame.getTitle());
        Mockito.verify(gameRepository).findById(mockGame.getId());
    }

    @Test
    @EnabledOnJre(JRE.JAVA_21)
    @DisplayName("Delete Game")
    void testDeleteGame() {
        Game mockGame = createDigitalGameMock(1, "Cyberpunk", GameCategory.ADVENTURE, 500_000, GameType.DIGITAL, 120_000);

        Mockito.when(gameRepository.findById(mockGame.getId()))
                .thenReturn(Optional.of(mockGame));

        Mockito.when(gameService.deleteGame(mockGame.getId()))
                .thenReturn(false);

        boolean result = gameService.deleteGame(mockGame.getId());

        Assertions.assertFalse(result);
        Mockito.verify(gameRepository).delete(mockGame.getId());
    }

    @Test
    @EnabledOnJre(JRE.JAVA_21)
    @DisplayName("Find All Games")
    void findAllGame() {
        DigitalGame game1 = createDigitalGameMock(1, "Cyberpunk 2077", GameCategory.ADVENTURE, 500_000, GameType.DIGITAL, 120_000);
        DigitalGame game2 = createDigitalGameMock(2, "The Witcher 3", GameCategory.RPG, 400_000, GameType.DIGITAL, 50_000);
        List<Game> expectedGames = List.of(game1, game2);

        Mockito.when(gameRepository.findAll()).thenReturn(expectedGames);

        List<Game> actualGames = gameService.findAllGame();

        Assertions.assertNotNull(actualGames);
        Assertions.assertEquals(2, actualGames.size());
        Assertions.assertEquals(game1.getTitle(), actualGames.get(0).getTitle());
        Mockito.verify(gameRepository).findAll();
    }

    @Test
    @DisplayName("Update Game")
    void updateGame() {
        DigitalGame gameToUpdate = createDigitalGameMock(1, "Elden Ring Reforged", GameCategory.RPG, 750_000, GameType.DIGITAL, 60_000);

        Mockito.when(gameRepository.update(gameToUpdate)).thenReturn(gameToUpdate);

        Game updatedGame = gameService.updateGame(gameToUpdate);

        Assertions.assertNotNull(updatedGame);
        Assertions.assertEquals(gameToUpdate.getId(), updatedGame.getId());
        Assertions.assertEquals("Elden Ring Reforged", updatedGame.getTitle());
        Assertions.assertEquals(750_000, updatedGame.getPrice());
        Mockito.verify(gameRepository).update(gameToUpdate);
    }

    @Test
    @DisplayName("Update Game - Failed / Not Found")
    void updateGameNotFound() {
        DigitalGame nonExistentGame = createDigitalGameMock(99, "Unknown Game", GameCategory.ACTION, 100_000, GameType.DIGITAL, 10_000);

        Mockito.when(gameRepository.update(nonExistentGame)).thenThrow(new RuntimeException("Game not found"));

        Assertions.assertThrows(RuntimeException.class, () -> {
            gameService.updateGame(nonExistentGame);
        });

        Mockito.verify(gameRepository).update(nonExistentGame);
    }
}
