package mana.game.shop.view;

import mana.game.shop.entity.*;
import mana.game.shop.service.GameService;
import mana.game.shop.service.TransactionService;
import mana.game.shop.service.UserService;
import mana.game.shop.util.InputUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class AdminView {
    private UserService userService;
    private GameService gameService;
    private TransactionService transactionService;
    private final User currentUser;

    public AdminView(UserService userService, GameService gameService,  TransactionService transactionService, User currentUser) {
        this.currentUser = currentUser;
        this.transactionService = transactionService;
        this.gameService = gameService;
        this.userService = userService;
    }

    public void showMenu() {
        while (true) {
            System.out.println("==================================");
            System.out.println("Admin Menu");
            System.out.println("Welcome " + currentUser.getUsername());
            System.out.println("==================================");
            System.out.println("1. Add game");
            System.out.println("2. Edit game");
            System.out.println("3. Delete game");
            System.out.println("4. View all games");
            System.out.println("5. View all person");
            System.out.println("6. Ban/Unban user");
            System.out.println("7. Add Admin");
            System.out.println("0. Logout");

            String input = InputUtil.stringInput("Choice: ");

            switch (input){
                case "1" -> addGame();
                case "2" -> editGame();
                case "3" -> deleteGame();
                case "4" -> viewAllGames();
                case "5" -> viewAllPerson();
                case "6" -> banUser();
                case "7" -> addAdmin();
                case "0" -> {
                    return;
                }
                default -> throw new IllegalArgumentException("Please input a valid number!");
            }
        }
    }

    private void addGame() {
        GameCategory gameCategory = null;
        GameType gameType = null;
        Integer size = null;
        Integer stock = null;
        String title = InputUtil.stringInput("Input game title: ");
        int categoryInput = InputUtil.intInput("Select game category" +
                "\n1. Action\n2. RPG\n3. Sports \n4. Strategy\n5. Adventure\nSelect: ");
        BigDecimal price = InputUtil.bigDecimalInput("Input game price: ");
        int typeInput = InputUtil.intInput("Select game type" +
                "\n1. Digital\n2. Physical\nSelect: ");
        LocalDate dateInput = InputUtil.dateInput("Input release date with format yyyy-MM-dd: ");

        switch (categoryInput) {
            case 1 -> gameCategory = GameCategory.ACTION;
            case 2 -> gameCategory = GameCategory.RPG;
            case 3 -> gameCategory = GameCategory.SPORTS;
            case 4 -> gameCategory = GameCategory.STRATEGY;
            case 5 -> gameCategory = GameCategory.ADVENTURE;
            default -> throw new IllegalArgumentException("Please input a valid number!");
        }

        switch (typeInput) {
            case 1 -> {
                gameType = GameType.DIGITAL;
                size = InputUtil.intInput("Input game size in Mb: ");
            }
            case 2 -> {
                gameType = GameType.PHYSICAL;
                stock = InputUtil.intInput("Input game stock: ");
            }
            default -> throw new IllegalArgumentException("Please input a valid number!");
        }

        if (gameType.equals(GameType.PHYSICAL)) {
            PhysicalGame physicalGame = new PhysicalGame(title, gameCategory, price, gameType, stock, null, dateInput);
            Game addedGame = gameService.addGame(physicalGame);
            if (Objects.nonNull(addedGame)) {
                System.out.println("Success added game: " + addedGame.getTitle());
            } else {
                throw new RuntimeException("The game could not be added");
            }
        } else {
            DigitalGame digitalGame = new DigitalGame(title, gameCategory, price, gameType, null, size, dateInput);
            Game addedGame = gameService.addGame(digitalGame);
            if (Objects.nonNull(addedGame)) {
                System.out.println("Success added game: " + addedGame.getTitle());
            } else {
                throw new RuntimeException("The game could not be added");
            }
        }
    }

    private void editGame() {
        int gameId = InputUtil.intInput("Input game Id want to edit: ");
        Game selectedGame = gameService.findGame(gameId);

        if (selectedGame == null) {
            System.out.println("Game not found!");
            return;
        }

        boolean isPhysical = selectedGame.getGameType().equals(GameType.PHYSICAL);

        System.out.println("\nSelected game: " + selectedGame.getTitle());
        System.out.println("Which section would you like to edit?");
        System.out.println("1. Edit game title");
        System.out.println("2. Edit game category");
        System.out.println("3. Edit game price");
        System.out.println("4. " + (isPhysical ? "Edit game stock" : "Edit game size"));
        System.out.println("5. Edit game release date");
        System.out.println("0. Cancel");

        int input = InputUtil.intInput("Select: ");

        switch (input) {
            case 1 -> {
                String newTitle = InputUtil.stringInput("Input new title: ");
                selectedGame.setTitle(newTitle);
            }
            case 2 -> {
                String newCategory = InputUtil.stringInput("Input new category: ");
                selectedGame.setGameCategory(GameCategory.valueOf(newCategory.toUpperCase()));
            }
            case 3 -> {
                BigDecimal newPrice = InputUtil.bigDecimalInput("Input new price: ");
                selectedGame.setPrice(newPrice);
            }
            case 4 -> {
                if (isPhysical) {
                    int newStock = InputUtil.intInput("Input new stock: ");
                    selectedGame.setStock(newStock);
                } else {
                    int newSize = InputUtil.intInput("Input new size (MB): ");
                    selectedGame.setSize(newSize);
                }
            }
            case 5 -> {
                LocalDate newDate = InputUtil.dateInput("Input new release date");
                selectedGame.setRelease_date(newDate);
            }
            case 0 -> {
                System.err.println("Update cancelled.");
                return;
            }
            default -> {
                System.err.println("Invalid option!");
                return;
            }
        }

        gameService.updateGame(selectedGame);
        System.out.println("Game " + selectedGame.getTitle() +  " successfully updated!");
    }

    private void deleteGame() {
        int id = InputUtil.intInput("Enter the game ID you want to delete: ");
        boolean result = gameService.deleteGame(id);
        if (result) {
            System.out.println("Game with Id " + id + " was deleted!");
        }
    }

    private void viewAllGames() {
        List<Game> games = gameService.findAllGame();
        if (games.isEmpty()) {
            throw new IllegalArgumentException("There are no games!");
        } else {
            for (Game game : games) {
                viewGame(game);
            }
        }
    }

    private void viewAllPerson() {
        System.out.println("1. View All Admin\n2. View All Users");
        int input = InputUtil.intInput("Select: ");
        List<User> users = null;
        switch (input) {
            case 1 -> {
                users = userService.getAll(UserRole.ADMIN);
            }
            case 2 -> {
                users = userService.getAll(UserRole.CUSTOMER);
            }
            default -> System.err.println("Please input a valid number!");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.of("Asia/Jakarta"));
        if (users.isEmpty()) {
            throw new IllegalArgumentException("There are no users!");
        } else {
            users.stream()
                    .sorted(Comparator.comparingInt(User::getId))
                    .forEach(user -> {
                        String updatedAt = user.getUpdated_at() != null
                                ? formatter.format(user.getUpdated_at())
                                : "-";
                        String createdAt = user.getCreated_at() != null
                                ? formatter.format(user.getCreated_at())
                                : "-";
                        System.out.println("========================================================");
                        System.out.println("User Id: " + user.getId());
                        System.out.println("Username: " + user.getUsername());
                        System.out.println("Password: " + user.getPassword());
                        System.out.println("Email: " + user.getEmail());
                        System.out.println("Role: " + user.getUserRole());
                        System.out.println("Balance: " + InputUtil.decimalFormat(user.getBalance()));
                        if (user.isIs_active()) {
                            System.out.println("Status: Active");
                        } else {
                            System.out.println("Status: Banned");
                        }
                        System.out.println("Updated At: " + updatedAt);
                        System.out.println("Created At: " + createdAt);
                        System.out.println("========================================================\n");

                    });
        }
    }

    private void banUser() {
        int id = InputUtil.intInput("Enter the ID user: ");
        User user = userService.findUser(id);
        if (!user.isIs_active()) {
            System.out.println("Current user status is: BANNED, and try to change into ACTIVE...");
            boolean activeUser = userService.banUser(id, currentUser.getId(), Boolean.TRUE);
            if (activeUser) {
                System.out.println("User with Id " + id + " was successfully active!");
            } else {
                throw new RuntimeException("Update failed!");
            }
        } else {
            System.out.println("Current user status is: ACTIVE, and try to change into BANNED...");
            boolean bannedUser = userService.banUser(id, currentUser.getId(), Boolean.FALSE);
            if (bannedUser) {
                System.out.println("User with Id " + id + " was successfully banned!");
            } else {
                throw new RuntimeException("Update failed!");
            }
        }
    }

    public void addAdmin(){
        System.out.println("ADD ADMIN PAGE");
        String username = InputUtil.stringInput("Input username: ");
        if (username.isBlank()) {
            throw new IllegalArgumentException("Please input the username!");
        } else {
            try {
                User newAdmin = new User(username, username, username + "@mail.com", UserRole.ADMIN);
                userService.addUser(newAdmin);
                System.out.println("Admin with username: " + newAdmin.getUsername() + " successfully created!");
            } catch (RuntimeException exception) {
                throw new RuntimeException(exception);
            }
        }
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
