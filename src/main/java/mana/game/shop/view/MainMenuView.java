package mana.game.shop.view;

import mana.game.shop.entity.User;
import mana.game.shop.entity.UserRole;
import mana.game.shop.service.GameService;
import mana.game.shop.service.TransactionService;
import mana.game.shop.service.UserService;
import mana.game.shop.util.InputUtil;

import java.sql.SQLException;

public class MainMenuView {
    private UserService userService;
    private GameService gameService;
    private TransactionService transactionService;

    public MainMenuView(UserService userService, GameService gameService, TransactionService transactionService) {
        this.userService = userService;
        this.gameService = gameService;
        this.transactionService = transactionService;
    }

    public void showMainMenu() throws SQLException {
        while (true) {
            System.out.println("==================================");
            System.out.println("Welcome to Mana Gameshop!");
            System.out.println("==================================");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("0. Exit");

            int input = InputUtil.intInput("Select: ");

            switch (input){
                case 1-> registerMenu();
                case 2 -> loginMenu();
                case 0 -> {
                    return;
                }
                default -> throw new IllegalArgumentException("Please input a valid number!");
            }
        }
    }

    public void registerMenu() {
        System.out.println("REGISTER PAGE");
        String username = InputUtil.stringInput("Input your username: ");
        String password = InputUtil.stringInput("Input your password: ");
        String email = InputUtil.stringInput("Input your email: ");

        if (username.isBlank() || password.isBlank() || email.isBlank()) {
            System.err.println("Error: Username, Password, and Email cannot be empty!");
            return;
        }

        try {
            User newUser = new User(username, password, email, UserRole.CUSTOMER);
            User createdUser = userService.addUser(newUser);

            if (createdUser == null) {
                System.err.println("Failed to create user. Please try again.");
                return;
            }

            System.out.println("User " + createdUser.getUsername() + " was successfully created!");
        } catch (Exception e) {
            System.err.println("Registration failed: " + e.getMessage());
        }
    }

    public void loginMenu() throws SQLException {
        System.out.println("LOGIN PAGE");
        String username = InputUtil.stringInput("Input your username: ");
        String password = InputUtil.stringInput("Input your password: ");

        if (username.isBlank() || password.isBlank()){
            throw new RuntimeException("Please fill up username and password!");
        }
        User currentUser = userService.authenticate(username, password);
        if (currentUser.getUsername().isBlank()){
            throw new RuntimeException("Failed to login!");
        } else if(currentUser.getUserRole().equals(UserRole.ADMIN)) {
            AdminView adminView = new AdminView(userService, gameService, transactionService, currentUser);
            adminView.showMenu();
        } else {
            CustomerView customerView = new CustomerView(userService, gameService, transactionService, currentUser);
            customerView.showMenu();
        }
    }
}
