package mana.game.shop.view;

import mana.game.shop.entity.*;
import mana.game.shop.exception.BusinessException;
import mana.game.shop.exception.ResourceNotFoundException;
import mana.game.shop.service.GameService;
import mana.game.shop.service.TransactionService;
import mana.game.shop.service.UserService;
import mana.game.shop.util.ColorUtil;
import mana.game.shop.util.DatabaseUtil;
import mana.game.shop.util.DateUtil;
import mana.game.shop.util.InputUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomerView {
    private final UserService userService;
    private final GameService gameService;
    private final TransactionService transactionService;
    private User currentUser;

    public CustomerView(UserService userService, GameService gameService,  TransactionService transactionService, User currentUser) {
        this.currentUser = currentUser;
        this.transactionService = transactionService;
        this.gameService = gameService;
        this.userService = userService;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n==================================");
            System.out.println("Customer Menu");
            System.out.println("Welcome " + currentUser.getUsername());
            System.out.println("==================================");
            System.out.println("1. Top up balance");
            System.out.println("2. Buy game");
            System.out.println("3. Refund game (Refunds are only available for transactions from the last 7 days)");
            System.out.println("4. Search game");
            System.out.println("5. Edit profile");
            System.out.println("6. See profile");
            System.out.println("7. Transaction history");

            System.out.println("0. Logout");

            String input = InputUtil.stringInput("Choice: ");

            switch (input){
                case "1" -> topup();
                case "2" -> buyGame();
                case "3" -> refundGame();
                case "4" -> searchGame();
                case "5" -> editProfile();
                case "6" -> seeProfile();
                case "7" -> displayUserTransactions(currentUser);
                case "0" -> {
                    return;
                }
                default -> ColorUtil.printError("Please input a valid number!");
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

    private void buyGame() {
        List<Game> games = gameService.findAllGame();
        for (Game game : games) {
            viewGame(game);
        }

        int gameId = InputUtil.intInput("Input Id game you want to buy: ");

        try {
            boolean result = transactionService.buyGame(currentUser.getId(), gameId);
            if (result) {
                currentUser = userService.findUser(currentUser.getId());

                System.out.println("Success bought game!");
                System.out.println("Now you have a game: " + gameService.findGame(gameId).getTitle());
                System.out.println("And your balance is: Rp. " + InputUtil.decimalFormat(currentUser.getBalance()));
            }
        } catch (BusinessException exception) {
            ColorUtil.printError("An error occurred: " + exception.getMessage());
        }
    }

    private void refundGame() {
        try {
            filterUserTransaction(currentUser);

            int input = InputUtil.intInput("Please select the id transaction you wish to refund (0 for back): ");

            if (input == 0) {
                return;
            }

            try {
                boolean result = transactionService.refundTransaction(input, currentUser.getId());
                if (result) {
                    ColorUtil.printSuccess("Success to refund transaction! Balance Rp. "
                            + InputUtil.decimalFormat(transactionService.findTransaction(input).getAmount())
                            + " and game " + gameService.findGame(transactionService.findTransaction(input).getGameId()).getTitle()
                            + " refund successful!");
                }
            } catch (BusinessException exception) {
                ColorUtil.printError("An error occurred: " + exception.getMessage());
            }
        } catch (ResourceNotFoundException exception) {
            ColorUtil.printError("Failed to retrieve user transaction: " + exception.getMessage());
        }
    }

    private void searchGame() {
        String name = InputUtil.stringInput("Input the name of game went you search: ");
        List<Game> games = gameService.findGamebyName(name);
        System.out.println(games.size() + " result for game with name " + name + " : ");
        games.forEach(this::viewGame);
    }

    private void editProfile() {
        System.out.println("\n=== EDIT PROFILE ===");
        System.out.println("1. Edit Username (Current: " + currentUser.getUsername() + ")");
        System.out.println("2. Edit Email    (Current: " + currentUser.getEmail() + ")");
        System.out.println("3. Change Password");
        System.out.println("0. Back");

        String choice = InputUtil.stringInput("Select: ");

        switch (choice) {
            case "1" -> {
                String newUsername = InputUtil.stringInput("Input new username: ");
                currentUser.setUsername(newUsername);
            }
            case "2" -> {
                String newEmail = InputUtil.stringInput("Input new email: ");
                currentUser.setEmail(newEmail);
            }
            case "3" -> {
                String newPassword = InputUtil.stringInput("Input new password: ");
                currentUser.setPassword(newPassword);
            }
            case "0" -> {
                return;
            }
            default -> {
                System.out.println("Invalid option!");
                return;
            }
        }

        try {
            User updatedUser = userService.updateUser(currentUser);
            if (updatedUser.getUsername().equalsIgnoreCase(currentUser.getUsername())) {
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
        System.out.println("Balance: " + "Rp. " + InputUtil.decimalFormat(currentUser.getBalance()));
        if (currentUser.isIs_active()) {
            System.out.println("Status: Active");
        } else {
            System.out.println("Status: Banned");
        }
        List<Transaction> transactions = transactionService.findTransactionById(currentUser.getId()).stream().filter(
                transaction -> {
                    return transaction.getStatus().equals(TransactionStatus.SUCCESS);
                }
        ).toList();
        List<Game> games = transactions.stream()
                .map(transaction -> gameService.findGame(transaction.getGameId()))
                .toList();
        if (!transactions.isEmpty()) {
            System.out.print("Games owned: ");
            games.forEach(game -> System.out.print(game.getTitle() + " " + game.getGameType() + " VERSION,\n"));
        }
        System.out.println("Member since: " + DateUtil.formatDate(currentUser.getCreated_at()));
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
            System.out.println("Game size     : " + InputUtil.sizeInGB(game.getSize()) + " GB");
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
            System.out.println("\n=========================== This Month =============================");
            printTransactionList(thisMonthTransactions);
        }

        if (olderTransactions.isEmpty()) {
            System.out.println("====================================================================\n");
        } else {
            System.out.println("\n=========================== Other Months ===========================");
            printTransactionList(olderTransactions);
        }
    }

    private void filterUserTransaction(User currentUser) {
        List<Transaction> transactions = transactionService.filteredFindTransactionById(currentUser.getId());

        if (transactions.isEmpty()) {
            throw new ResourceNotFoundException("No transaction history found.");
        }

        transactions.forEach(transaction -> {
            System.out.println("\n==================================");
            System.out.println("Transaction id: " + transaction.getId());
            System.out.println("Purchased games: " + gameService.findGame(transaction.getGameId()).getTitle());
            System.out.println("Amount: " + InputUtil.decimalFormat(transaction.getAmount()));
            System.out.println("Status: " + transaction.getStatus().getDisplayName());
            System.out.println("Date of purchase: " + DateUtil.formatDate(transaction.getTransactionDate()));
            System.out.println("==================================\n");
        });

    }

    private void printTransactionList(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            Game game = gameService.findGame(transaction.getGameId());

            System.out.println("\n========================================");
            System.out.println("Game: " + game.getTitle());
            System.out.println("Price: Rp. " + InputUtil.decimalFormat(game.getPrice()));
            System.out.println("Status: " + transaction.getStatus().getDisplayName());
            System.out.println("Purchase date: " + DateUtil.formatDate(transaction.getTransactionDate()));
            System.out.println("========================================\n");
        }
    }
}
