package mana.game.shop.view;

import mana.game.shop.entity.Game;
import mana.game.shop.entity.GameType;
import mana.game.shop.entity.User;
import mana.game.shop.service.GameService;
import mana.game.shop.service.TransactionService;
import mana.game.shop.service.UserService;
import mana.game.shop.util.CurrencyUtil;
import mana.game.shop.util.InputUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class CustomerView {
    private UserService userService;
    private GameService gameService;
    private TransactionService transactionService;
    private User currentUser;

    public CustomerView(UserService userService, GameService gameService,  TransactionService transactionService, User currentUser) {
        this.currentUser = currentUser;
        this.transactionService = transactionService;
        this.gameService = gameService;
        this.userService = userService;
    }

    public void showMenu() throws SQLException {
        while (true) {
            System.out.println("==================================");
            System.out.println("Customer Menu");
            System.out.println("Welcome " + currentUser.getUsername());
            System.out.println("==================================");
            System.out.println("1. Topup");
            System.out.println("2. Buy game");
            System.out.println("3. Edit profile");
            System.out.println("4. See profile");
            System.out.println("0. Logout");

            int input = InputUtil.intInput("Choice: ");

            switch (input){
                case 1 -> topup();
                case 2 -> buyGame();
                case 3 -> editProfile();
                case 4 -> seeProfile();
                case 0 -> {
                    return;
                }
                default -> throw new IllegalArgumentException("Please input a valid number!");
            }
        }
    }

    private void topup() {
        double addedSaldo = InputUtil.doubleInput("Input saldo: ");
        if (addedSaldo <= 0) {
            System.out.println("Nominal topup harus lebih dari 0!");
            return;
        }

        boolean result = userService.addBalance(currentUser.getId(), addedSaldo);
        if (result) {
            currentUser = userService.findUser(currentUser.getId());

            System.out.println("Topup successfully!");
            System.out.println("Rp. " + InputUtil.decimalFormat(addedSaldo) + " was added into your account!");
            System.out.println("Current Balance: Rp. " + InputUtil.decimalFormat(currentUser.getBalance()));
        }
    }

    private void buyGame() throws SQLException {
        List<Game> games = gameService.findAllGame();
        for (Game game : games) {
            viewGame(game);
        }

        int gameId = InputUtil.intInput("Input Id game you want to buy: ");
        boolean result = transactionService.buyGame(currentUser.getId(), gameId);

        if (result) {
            currentUser = userService.findUser(currentUser.getId());

            System.out.println("Success bought game!");
            System.out.println("Now you have a game: " + gameService.findGame(gameId).getTitle());
            System.out.println("And your balance is: Rp. " + InputUtil.decimalFormat(currentUser.getBalance()));
        }
    }

    private void editProfile() {
        System.out.println("\n=== EDIT PROFILE ===");
        System.out.println("1. Edit Username (Current: " + currentUser.getUsername() + ")");
        System.out.println("2. Edit Email    (Current: " + currentUser.getEmail() + ")");
        System.out.println("3. Change Password");
        System.out.println("0. Back");

        int choice = InputUtil.intInput("Select: ");

        switch (choice) {
            case 1 -> {
                String newUsername = InputUtil.stringInput("Input new username: ");
                currentUser.setUsername(newUsername);
            }
            case 2 -> {
                String newEmail = InputUtil.stringInput("Input new email: ");
                currentUser.setEmail(newEmail);
            }
            case 3 -> {
                String newPassword = InputUtil.stringInput("Input new password: ");
                currentUser.setPassword(newPassword);
            }
            case 0 -> {
                return;
            }
            default -> {
                System.out.println("Invalid option!");
                return;
            }
        }

        try {
            boolean updated = userService.updateUser(currentUser);
            if (updated) {
                System.out.println("Profile updated successfully!");
            } else {
                System.out.println("Failed to update profile.");
            }
        } catch (Exception e) {
            System.out.println("Error updating profile: " + e.getMessage());
        }
    }

    private void seeProfile() {
        System.out.println("========================================================");
        System.out.println("Username: " + currentUser.getUsername());
        System.out.println("Password: " + currentUser.getPassword());
        System.out.println("Email: " + currentUser.getEmail());
        System.out.println("Balance: " + CurrencyUtil.toRupiahNumber(currentUser.getBalance()));
        if (currentUser.isIs_active()) {
            System.out.println("Status: Active");
        } else {
            System.out.println("Status: Banned");
        }
        System.out.println("========================================================\n");
    }

    private void viewGame(Game game) {
        System.out.println("\n========================================================");
        System.out.println("Game Id       : " + game.getId());
        System.out.println("Game title    : " + game.getTitle());
        System.out.println("Game category : " + game.getGameCategory());
        System.out.println("Game price    : Rp. " + InputUtil.decimalFormat(game.getPrice()));
        System.out.println("Game type     : " + game.getGameType());

        if (game.getGameType() == GameType.PHYSICAL) {
            System.out.println("Game stock    : " + game.getStock() + " unit");
        } else {
            System.out.println("Game size     : " + game.getSize() + " MB");
        }

        System.out.println("Release date  : " + game.getRelease_date());
        System.out.println("========================================================\n");
    }
}
