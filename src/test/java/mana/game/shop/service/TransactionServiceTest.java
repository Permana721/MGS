package mana.game.shop.service;

import com.zaxxer.hikari.HikariDataSource;
import mana.game.shop.entity.Game;
import mana.game.shop.entity.Transaction;
import mana.game.shop.entity.User;
import mana.game.shop.repository.*;
import mana.game.shop.util.DatabaseUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransactionServiceTest {
    TransactionService transactionService;
    UserService userService;
    GameService gameService;

    @BeforeEach
    void setUp() {
        HikariDataSource dataSource = DatabaseUtil.getDataSource();
        UserRepository userRepository = new UserRepositoryImpl(dataSource);
        userService = new UserServiceImpl(userRepository);
        GameRepository gameRepository = new GameRepositoryImpl(dataSource);
        gameService = new GameServiceImpl(gameRepository);
        TransactionRepository transactionRepository = new TransactionRepositoryImpl(dataSource);
        transactionService = new TransactionServiceImpl(transactionRepository, gameRepository, userRepository);
    }

    @Test
    void testBuyGame() throws SQLException {
        boolean result = transactionService.buyGame(1, 7);
        System.out.println(result);
    }

    @Test
    void testFindUserGames() {
        int userId = 1;
        List<Transaction> transactions = transactionService.findTransactionById(userId);

        List<Game> ownedGames = new ArrayList<>();

        for (Transaction transaction : transactions) {
            Game game = gameService.findGame(transaction.getGameId());
            ownedGames.add(game);
        }

        System.out.println("List of games owned by the user " + userService.findUser(userId).getUsername());
        for (Game game : ownedGames) {
            System.out.println("ID: " + game.getId() + " | Name: " + game.getTitle() + " | Tipe: " + game.getGameType());
        }

        Assertions.assertFalse(ownedGames.isEmpty());
    }
}
