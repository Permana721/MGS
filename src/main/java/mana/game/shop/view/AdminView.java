package mana.game.shop.view;

import mana.game.shop.entity.*;
import mana.game.shop.exception.BusinessException;
import mana.game.shop.exception.BusinessValidationException;
import mana.game.shop.exception.ResourceNotFoundException;
import mana.game.shop.service.CouponService;
import mana.game.shop.service.GameService;
import mana.game.shop.service.TransactionService;
import mana.game.shop.service.UserService;
import mana.game.shop.util.ColorUtil;
import mana.game.shop.util.DateUtil;
import mana.game.shop.util.InputUtil;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class AdminView {
    private final UserService userService;
    private final GameService gameService;
    private final TransactionService transactionService;
    private final CouponService couponService;
    private final User currentUser;

    public AdminView(UserService userService, GameService gameService, TransactionService transactionService, CouponService couponService, User currentUser) {
        this.currentUser = currentUser;
        this.gameService = gameService;
        this.transactionService = transactionService;
        this.couponService = couponService;
        this.userService = userService;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n==================================");
            System.out.println("Admin Menu");
            System.out.println("Welcome " + currentUser.getUsername());
            System.out.println("==================================");
            System.out.println("1. Game Management");
            System.out.println("2. User Management");
            System.out.println("3. Transaction History");
            System.out.println("4. Cupon Management");
            System.out.println("0. Logout");

            String input = InputUtil.stringInput("Choice: ");

            switch (input){
                case "1" -> gameManagement();
                case "2" -> userManagement();
                case "3" -> transactionHistory();
                case "4" -> couponManagement();
                case "0" -> {
                    return;
                }
                default -> ColorUtil.printError("Please input a valid number!");
            }
        }
    }

    private void gameManagement() {
        System.out.println("\n============Game Management============");
        System.out.println("1. Add game");
        System.out.println("2. Edit game");
        System.out.println("3. Delete game");
        System.out.println("4. View all game");
        System.out.println("0. Back");
        int input = InputUtil.intInput("Choice: ");

        switch (input) {
            case 1 -> addGame();
            case 2 -> editGame();
            case 3 -> deleteGame();
            case 4 -> viewAllGames();
            case 0 -> showMenu();
            default -> ColorUtil.printError("Please input a valid number!");
        }
    }

    private void addGame() {
        GameCategory gameCategory = null;
        GameType gameType = null;
        Integer size = null;
        Integer stock = null;
        String title = InputUtil.stringInput("Input game title: ");
        String categoryInput = InputUtil.stringInput("""
                Select game category
                1. Action
                2. RPG
                3. Sports\s
                4. Strategy
                5. Adventure
                Select:\s""");
        BigDecimal price = InputUtil.bigDecimalInput("Input game price: ");
        String typeInput = InputUtil.stringInput("""
                Select game type
                1. Digital
                2. Physical
                Select:\s""");
        LocalDate dateInput = DateUtil.dateInput("Input release date with format yyyy-MM-dd: ");

        switch (categoryInput) {
            case "1" -> gameCategory = GameCategory.ACTION;
            case "2" -> gameCategory = GameCategory.RPG;
            case "3" -> gameCategory = GameCategory.SPORTS;
            case "4" -> gameCategory = GameCategory.STRATEGY;
            case "5" -> gameCategory = GameCategory.ADVENTURE;
            default -> ColorUtil.printError("Please input a valid number!");
        }

        switch (typeInput) {
            case "1" -> {
                gameType = GameType.DIGITAL;
                size = InputUtil.intInput("Input game size in Mb: ");
            }
            case "2" -> {
                gameType = GameType.PHYSICAL;
                stock = InputUtil.intInput("Input game stock: ");
            }
            default -> ColorUtil.printError("Please input a valid number!");
        }

        assert gameType != null;
        if (gameType.equals(GameType.PHYSICAL)) {
            PhysicalGame physicalGame = new PhysicalGame(title, gameCategory, price, gameType, stock, null, dateInput);
            try {
                gameService.addGame(physicalGame);
                ColorUtil.printSuccess("Success added game: " + physicalGame.getTitle());
            } catch (BusinessValidationException exception) {
                ColorUtil.printError("Failed to add a game: " + exception.getMessage());
            } catch (Exception exception) {
                ColorUtil.printError("System error occurred: " + exception.getMessage());
            }
        } else {
            DigitalGame digitalGame = new DigitalGame(title, gameCategory, price, gameType, null, size, dateInput);
            try {
                gameService.addGame(digitalGame);
                ColorUtil.printSuccess("Success added game: " + digitalGame.getTitle());
            } catch (BusinessValidationException exception) {
                ColorUtil.printError("Failed to add a game: " + exception.getMessage());
            } catch (Exception exception) {
                ColorUtil.printError("System error occurred: " + exception.getMessage());
            }
        }
    }

    private void editGame() {
        viewAllGames();
        int gameId = InputUtil.intInput("Input game Id want to edit: ");
        try {
            Game selectedGame = gameService.findGame(gameId);

            boolean isPhysical = selectedGame.getGameType().equals(GameType.PHYSICAL);

            System.out.println("\nSelected game: " + selectedGame.getTitle());
            System.out.println("Which section would you like to edit?");
            System.out.println("1. Edit game title");
            System.out.println("2. Edit game category");
            System.out.println("3. Edit game price");
            System.out.println("4. " + (isPhysical ? "Edit game stock" : "Edit game size"));
            System.out.println("5. Edit game release date");
            System.out.println("0. Cancel");

            String input = InputUtil.stringInput("Select: ");

            switch (input) {
                case "1" -> {
                    String newTitle = InputUtil.stringInput("Input new title: ");
                    selectedGame.setTitle(newTitle);
                }
                case "2" -> {
                    String newCategory = InputUtil.stringInput("Input new category: ");
                    selectedGame.setGameCategory(GameCategory.valueOf(newCategory.toUpperCase()));
                }
                case "3" -> {
                    BigDecimal newPrice = InputUtil.bigDecimalInput("Input new price: ");
                    selectedGame.setPrice(newPrice);
                }
                case "4" -> {
                    if (isPhysical) {
                        int newStock = InputUtil.intInput("Input new stock: ");
                        selectedGame.setStock(newStock);
                    } else {
                        int newSize = InputUtil.intInput("Input new size (MB): ");
                        selectedGame.setSize(newSize);
                    }
                }
                case "5" -> {
                    LocalDate newDate = DateUtil.dateInput("Input new release date");
                    selectedGame.setRelease_date(newDate);
                }
                case "0" -> {
                    ColorUtil.printError("Update cancelled.");
                    return;
                }
                default -> {
                    ColorUtil.printError("Invalid option!");
                    return;
                }
            }

            gameService.updateGame(selectedGame);

            ColorUtil.printSuccess("Game " + selectedGame.getTitle() +  " successfully updated!");
        } catch (ResourceNotFoundException exception) {
            ColorUtil.printError(exception.getMessage());
        }
    }

    private void deleteGame() {
        viewAllGames();
        int id = InputUtil.intInput("Enter the game ID you want to delete: ");
        try {
            gameService.deleteGame(id);
                ColorUtil.printSuccess("Game with Id " + id + " was deleted!");
        } catch (ResourceNotFoundException exception) {
            ColorUtil.printError(exception.getMessage());
        } catch (Exception exception) {
            ColorUtil.printError("System error occurred. Please try again later!");
        }
    }

    private void viewAllGames() {
        try {
            List<Game> games = gameService.findAllGame();
            for (Game game : games) {
                viewGame(game);
            }
        } catch (ResourceNotFoundException exception) {
            ColorUtil.printError("No game data: " + exception.getMessage());
        } catch (Exception exception) {
            ColorUtil.printError("System error occurred. Please try again later!");
        }
    }

    private void userManagement() {
        System.out.println("\n============User Management============");
        System.out.println("1. View all user");
        System.out.println("2. Ban user");
        System.out.println("3. Add admin");
        System.out.println("0. Back");
        int input = InputUtil.intInput("Choice: ");

        switch (input) {
            case 1 -> viewAllPerson();
            case 2 -> banUser();
            case 3 -> addAdmin();
            case 0 -> showMenu();
            default -> ColorUtil.printError("Please input a valid number!");
        }
    }

    private void viewAllPerson() {
        System.out.println("1. View All Admin\n2. View All Users");
        List<User> users = null;

        String input = InputUtil.stringInput("Select: ");
        switch (input) {
            case "1" -> users = userService.getAll(UserRole.ADMIN);
            case "2" -> users = userService.getAll(UserRole.CUSTOMER);
            default -> ColorUtil.printError("Please input a valid number!");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.of("Asia/Jakarta"));

        assert users != null;
        if (users.isEmpty()) {
            ColorUtil.printError("There are no users!");
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
                        System.out.println("Balance: " + "Rp. " + InputUtil.decimalFormat(user.getBalance()));
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
        try {
            User user = userService.findUser(id);
            if (!user.isIs_active()) {
                System.out.println("Current user status is: BANNED, and try to change into ACTIVE...");
                try {
                    userService.banUser(currentUser.getId(), id, Boolean.TRUE);
                    ColorUtil.printSuccess("User with Id " + id + " was successfully active!");
                } catch (BusinessException exception) {
                    ColorUtil.printError(exception.getMessage());
                }
            } else {
                System.out.println("Current user status is: ACTIVE, and try to change into BANNED...");
                try {
                    userService.banUser(currentUser.getId(), id, Boolean.FALSE);
                    ColorUtil.printSuccess("User with Id " + id + " was successfully banned!");
                } catch (BusinessException exception) {
                    ColorUtil.printError(exception.getMessage());
                }
            }
        } catch (BusinessException exception) {
            ColorUtil.printError("An error occurred: " + exception.getMessage());
        }
    }

    public void addAdmin(){
        System.out.println("ADD ADMIN PAGE");
        String username = InputUtil.stringInput("Input username: ");
        if (username.isBlank()) {
            ColorUtil.printError("Username cannot set to be NULL!");
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

    private void transactionHistory() {
        LocalDate fromDate = DateUtil.dateInput("Enter the transaction history dates from the date: ");
        LocalDate toDate = DateUtil.dateInput("Enter the transaction history dates up to the date: ");
        ZoneId zone = ZoneId.systemDefault();
        Instant fromInstant = fromDate.atStartOfDay(zone).toInstant();
        Instant toInstant = toDate.atTime(LocalTime.MAX).atZone(zone).toInstant();
        try {
            List<Transaction> transactions = transactionService.findTransactionHistory(fromInstant, toInstant);
            transactions.forEach(this::viewTransaction);
        } catch (BusinessException exception) {
            ColorUtil.printError("An error occurred: " + exception.getMessage());
        }
    }

    private void couponManagement() {
        System.out.println("\n============Coupon Management============");
        System.out.println("1. Add coupon");
        System.out.println("2. Edit coupon");
        System.out.println("3. Delete coupon");
        System.out.println("4. Nonactive coupon");
        System.out.println("5. Activate coupon");
        System.out.println("6. View all coupon");
        System.out.println("0. Back");
        int input = InputUtil.intInput("Choice: ");

        switch (input) {
            case 1 -> addCoupon();
            case 2 -> editCoupon();
            case 3 -> deleteCoupon();
            case 4 -> nonactiveCoupon();
            case 5 -> activeCoupon();
            case 6 -> viewAllCoupon();
            case 0 -> showMenu();
            default -> ColorUtil.printError("Please input a valid number!");
        }
    }

    private void addCoupon() {
        String code = InputUtil.stringInput("Please input coupon code: ");
        BigDecimal discount_percent = InputUtil.bigDecimalInput("Please input discount percent: ");
        BigDecimal max_discount = InputUtil.bigDecimalInput("Please input max discount: ");
        BigDecimal min_purchase = InputUtil.bigDecimalInput("Please input min purchase: ");
        int usage_limit = InputUtil.intInput("Please input usage limit: ");
        Instant valid_from = DateUtil.instantInput("Please input coupon valid from");
        Instant valid_until = DateUtil.instantInput("Please input coupon valid until");
        Coupon coupon = new Coupon(code, discount_percent, max_discount, min_purchase, usage_limit, Instant.from(valid_from), Instant.from(valid_until));

        try {
            couponService.addCoupon(coupon);
            ColorUtil.printSuccess("Success to save coupon with code: " + coupon.getCode());
        } catch (Exception exception) {
            ColorUtil.printError(exception.getMessage());
        }
    }

    private void editCoupon() {
        viewAllCoupon();
        String code = InputUtil.stringInput("Please input coupon code: ");
        Coupon coupon = couponService.searchCouponByCode(code);
        System.out.println("1. Edit coupon code \n2. Edit discount percent \n3. Edit max discount \n4. Edit min purchase \n5. Edit usage limit \n6. Edit valid from \n7. Edit valid until \n0. Back");
        int input = InputUtil.intInput("Choice: ");
        switch (input) {
            case 1 -> {
                String newCode = InputUtil.stringInput("Please input new coupon code: ");
                coupon.setCode(newCode);
            }
            case 2 -> {
                BigDecimal newDiscountPercent = InputUtil.bigDecimalInput("Please input new discount percent coupon: ");
                coupon.setDiscount_percent(newDiscountPercent);
            }
            case 3 -> {
                BigDecimal newMaxDiscount = InputUtil.bigDecimalInput("Please input new max discount coupon: ");
                coupon.setMax_discount(newMaxDiscount);
            }
            case 4 -> {
                BigDecimal newMinPurchase = InputUtil.bigDecimalInput("Please input new min purchase coupon: ");
                coupon.setMin_purchase(newMinPurchase);
            }
            case 5 -> {
                int newUsageLimit = InputUtil.intInput("Please input new usage limit coupon: ");
                coupon.setUsage_limit(newUsageLimit);
            }
            case 6 -> {
                Instant newValidFrom = DateUtil.instantInput("Please input new valid from coupon");
                coupon.setValid_from(newValidFrom);
            }
            case 7 -> {
                Instant newValidUntil = DateUtil.instantInput("Please input new valid until coupon");
                coupon.setValid_until(newValidUntil);
            }
            case 0 -> {
                return;
            }
            default -> ColorUtil.printError("Please input a valid number!");
        }

        try {
            couponService.updateCoupon(coupon);
            ColorUtil.printSuccess("Success to update coupon with code: " + coupon.getCode());
        } catch (Exception exception) {
            ColorUtil.printError(exception.getMessage());
        }
    }


    private void deleteCoupon() {
        String code = InputUtil.stringInput("Please input coupon code want to delete: ");
        try {
            couponService.deleteCoupon(code);
            ColorUtil.printSuccess("Success delete coupon with code: " + code);
        } catch (Exception exception) {
            ColorUtil.printError("An error occurred: " + code + " " + exception.getMessage());
        }
    }

    private void nonactiveCoupon() {
        String code = InputUtil.stringInput("Please input coupon code: ");
        try {
            couponService.deactivateCoupon(code);
            ColorUtil.printSuccess("Success to nonactive coupon: " + code);
        } catch (Exception exception) {
            ColorUtil.printError("An error occurred: " + exception.getMessage());
        }
    }

    private void activeCoupon() {
        String code = InputUtil.stringInput("Please input coupon code: ");
        try {
            couponService.activateCoupon(code);
            ColorUtil.printSuccess("Success to activate coupon: " + code);
        } catch (Exception exception) {
            ColorUtil.printError("An error occurred: " + exception.getMessage());
        }
    }

    private void viewAllCoupon() {
        List<Coupon> coupons;
        try {
            coupons = couponService.searchAllCoupon();

            coupons.forEach(this::viewCoupon);
        } catch (Exception exception) {
            ColorUtil.printError("An error occurred: " + exception.getMessage());
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
            System.out.println("Game size     : " + InputUtil.sizeInGB(game.getSize()) + " GB");
        }

        System.out.println("Release date  : " + game.getRelease_date());
        System.out.println("========================================================\n");
    }

    private void viewTransaction(Transaction transaction) {
        try {
            System.out.println("\n========================================================");
            System.out.println("Transaction Id    : " + transaction.getId());
            System.out.println("User Id           : " + transaction.getUserId() + " (" + userService.findUser(transaction.getUserId()).getUsername() + ")");
            System.out.println("Game Id           : " + transaction.getGameId() + " (" + gameService.findGame(transaction.getGameId()).getTitle() + ")");
            System.out.println("Price             : Rp. " + InputUtil.decimalFormat(transaction.getAmount()));
            System.out.println("Status            : " + transaction.getStatus().getDisplayName());
            System.out.println("Transaction Date  : " + DateUtil.formatDate(transaction.getTransactionDate()));
            System.out.println("========================================================\n");
        } catch (ResourceNotFoundException exception) {
            ColorUtil.printError("An error occurred: " + exception.getMessage());
        }
    }

    private void viewCoupon(Coupon coupon) {
        System.out.println("\n========================================================");
        System.out.println("Id: " + coupon.getId());
        System.out.println("Code: " + coupon.getCode());
        System.out.println("Discount percent: " + coupon.getDiscount_percent() + "%");
        System.out.println("Max discount: Rp. " + InputUtil.decimalFormat(coupon.getMax_discount()));
        System.out.println("Min purchase: Rp. " + InputUtil.decimalFormat(coupon.getMin_purchase()));
        System.out.println("Usage limit: " + coupon.getUsage_limit());
        System.out.println("Usage count: " + coupon.getUsed_count());
        System.out.println("Valid from: " + coupon.getValid_from());
        System.out.println("Valid until: " + coupon.getValid_until());
        System.out.println("Is active: " + coupon.isIs_active());
        System.out.println("Created_at: " + coupon.getCreated_at());
        System.out.println("Updated_at: " + coupon.getUpdated_at());
        System.out.println("========================================================\n");
    }
}
