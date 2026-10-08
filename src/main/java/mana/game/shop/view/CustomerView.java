package mana.game.shop.view;

import mana.game.shop.entity.Game;
import mana.game.shop.entity.GameType;
import mana.game.shop.entity.Transaction;
import mana.game.shop.entity.User;
import mana.game.shop.service.GameService;
import mana.game.shop.service.TransactionService;
import mana.game.shop.service.UserService;
import mana.game.shop.util.InputUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            System.out.println("1. Top up balance");
            System.out.println("2. Buy game");
            System.out.println("3. Search game");
            System.out.println("4. Edit profile");
            System.out.println("5. See profile");
            System.out.println("6. Transaction history");

            System.out.println("0. Logout");

            int input = InputUtil.intInput("Choice: ");

            switch (input){
                case 1 -> topup();
                case 2 -> buyGame();
                case 3 -> searchGame();
                case 4 -> editProfile();
                case 5 -> seeProfile();
                case 6 -> displayUserTransactions(currentUser);
                case 0 -> {
                    return;
                }
                default -> throw new IllegalArgumentException("Please input a valid number!");
            }
        }
    }

    private void topup() {
        BigDecimal addedSaldo = InputUtil.bigDecimalInput("Input saldo: ");
        if (addedSaldo.compareTo(BigDecimal.ZERO) == 0) {
            System.out.println("The top-up amount must be greater than 0!");
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

    private void searchGame() throws SQLException {
        String name = InputUtil.stringInput("Input the name of game went you search: ");
        List<Game> games = gameService.findGamebyName(name);
        System.out.println(games.size() + " result for game with name " + name + " : ");
        games.forEach(game -> System.out.print(game.getTitle() + "\n"));
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
        System.out.println("Balance: " + InputUtil.decimalFormat(currentUser.getBalance()));
        if (currentUser.isIs_active()) {
            System.out.println("Status: Active");
        } else {
            System.out.println("Status: Banned");
        }
        List<Transaction> transactions = transactionService.findTransactionById(currentUser.getId());
        List<Game> games = transactions.stream()
                .map(transaction -> gameService.findGame(transaction.getGameId()))
                .toList();
        if (!transactions.isEmpty()) {
            System.out.print("Games owned: ");
            games.forEach(game -> System.out.print(game.getTitle() + " " + game.getGameType() + " VERSION\n"));
        }
        System.out.println("Member since: " + InputUtil.formatDate(currentUser.getCreated_at()));
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

    public void displayUserTransactions(User currentUser) {
        List<Transaction> transactions = transactionService.findTransactionById(currentUser.getId());

        if (transactions.isEmpty()) {
            System.out.println("No transaction history found.");
            return;
        }

        ZoneId zoneId = ZoneId.of("Asia/Jakarta");
        YearMonth currentMonth = YearMonth.now(zoneId);

        Map<Boolean, List<Transaction>> partitioned = transactions.stream()
                .collect(Collectors.partitioningBy(tx -> {
                    YearMonth txMonth = YearMonth.from(tx.getTransactionDate().atZone(zoneId));
                    return txMonth.equals(currentMonth);
                }));

        List<Transaction> thisMonthTransactions = partitioned.getOrDefault(true, List.of());
        List<Transaction> olderTransactions = partitioned.getOrDefault(false, List.of());

        if (thisMonthTransactions.isEmpty()) {
            System.out.println("No transactions this month.");
        } else {
            System.out.println("\nThis Month =============================");
            printTransactionList(thisMonthTransactions);
        }

        if (olderTransactions.isEmpty()) {
            System.out.println("No older transactions.");
        } else {
            System.out.println("\nOther Months ===========================");
            printTransactionList(olderTransactions);
        }
    }

    private void printTransactionList(List<Transaction> list) {
        for (Transaction tx : list) {
            Game game = gameService.findGame(tx.getGameId());
            String formattedDate = InputUtil.formatDate(tx.getTransactionDate());
            String formattedPrice = InputUtil.decimalFormat(tx.getAmount());

            System.out.printf("- %s | Rp %s | Date: %s%n",
                    game.getTitle(), formattedPrice, formattedDate);
        }
    }
}
