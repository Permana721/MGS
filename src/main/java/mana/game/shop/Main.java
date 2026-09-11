package mana.game.shop;

import com.zaxxer.hikari.HikariDataSource;
import mana.game.shop.repository.*;
import mana.game.shop.service.*;
import mana.game.shop.util.DatabaseUtil;
import mana.game.shop.view.MainMenuView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    static void main(String[] args) throws SQLException {
        HikariDataSource dataSource = DatabaseUtil.getDataSource();
        UserRepository userRepository = new UserRepositoryImpl(dataSource);
        GameRepository gameRepository = new GameRepositoryImpl(dataSource);
        TransactionRepository transactionRepository = new TransactionRepositoryImpl(dataSource);

        UserService userService = new UserServiceImpl(userRepository);
        GameService gameService = new GameServiceImpl(gameRepository);
        TransactionService transactionService = new TransactionServiceImpl(transactionRepository, gameRepository, userRepository);
        MainMenuView mainMenuView = new MainMenuView(userService, gameService, transactionService);

        mainMenuView.showMainMenu();
    }
}