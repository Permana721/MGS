package mana.game.shop.view;

import mana.game.shop.entity.User;
import mana.game.shop.entity.UserRole;
import mana.game.shop.exception.AuthenticationException;
import mana.game.shop.exception.BusinessException;
import mana.game.shop.service.CouponService;
import mana.game.shop.service.GameService;
import mana.game.shop.service.TransactionService;
import mana.game.shop.service.UserService;
import mana.game.shop.util.ColorUtil;
import mana.game.shop.util.InputUtil;

import java.sql.SQLException;

public class MainMenuView {
    private UserService userService;
    private GameService gameService;
    private TransactionService transactionService;
    final private CouponService couponService;

    public MainMenuView(UserService userService, GameService gameService, TransactionService transactionService, CouponService couponService) {
        this.userService = userService;
        this.gameService = gameService;
        this.transactionService = transactionService;
        this.couponService = couponService;
    }

    public void showMainMenu() {
        while (true) {
            System.out.println("==================================");
            System.out.println("Welcome to Mana Gameshop!");
            System.out.println("==================================");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("0. Exit");

            String input = InputUtil.stringInput("Select: ");

            switch (input){
                case "1" -> registerMenu();
                case "2" -> loginMenu();
                case "0" -> {
                    return;
                }
                default -> ColorUtil.printError("Please input a valid number!");
            }
        }
    }

    public void registerMenu() {
        System.out.println("REGISTER PAGE");
        String username = InputUtil.stringInput("Input your username: ");
        String password = InputUtil.stringInput("Input your password: ");
        String email = InputUtil.stringInput("Input your email: ");

        if (username.isBlank()) {
            ColorUtil.printError("Error: Username cannot be empty!");
            return;
        }
        if (password.isBlank()) {
            ColorUtil.printError("Error: Password cannot be empty!");
            return;
        }
        if (email.isBlank()) {
            ColorUtil.printError("Error: Email cannot be empty!");
            return;
        }

        try {
            User newUser = new User(username, password, email, UserRole.CUSTOMER);
            userService.addUser(newUser);

            ColorUtil.printSuccess("User " + newUser.getUsername() + " was successfully created!");
        } catch (BusinessException exception) {
            ColorUtil.printError("Registration failed: " + exception.getMessage());
        } catch (Exception exception) {
            ColorUtil.printError("System error occurred: " + exception.getMessage());
        }
    }

    public void loginMenu() {
        System.out.println("LOGIN PAGE");
        String username = InputUtil.stringInput("Input your username: ");
        String password = InputUtil.stringInput("Input your password: ");

        if (username.isBlank() || password.isBlank()){
            ColorUtil.printError("Please fill up username and password!");
            return;
        }

        try {
            User currentUser = userService.authenticate(username, password);

            if(currentUser.getUserRole().equals(UserRole.ADMIN)) {
                AdminView adminView = new AdminView(userService, gameService, transactionService, couponService, currentUser);
                adminView.showMenu();
            } else {
                CustomerView customerView = new CustomerView(userService, gameService, transactionService, currentUser);
                customerView.showMenu();
            }
        } catch (BusinessException exception) {
            ColorUtil.printError("Login failed: " + exception.getMessage());
        } catch (Exception exception) {
            ColorUtil.printError("System error occurred: " + exception.getMessage());
        }
    }
}
