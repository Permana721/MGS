package mana.game.shop.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Scanner;

public class InputUtil {
    private static Scanner scanner = new Scanner(System.in);
    private static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(JAKARTA_ZONE);

    public static String stringInput(String info) {
        System.out.print(info + " ");
        String input = scanner.nextLine();
        return input != null ? input.trim() : "";
    }

    public static int intInput(String info) {
        System.out.print(info + " ");
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    public static double doubleInput(String info) {
        System.out.print(info + " ");
        double value = scanner.nextDouble();
        scanner.nextLine();
        return value;
    }

    public static BigDecimal bigDecimalInput(String info) {
        System.out.print(info + " ");
        BigDecimal value = BigDecimal.valueOf(scanner.nextDouble());
        scanner.nextLine();
        return value;
    }

    public static LocalDate dateInput(String info) {
        while (true) {
            System.out.print(info + " (format: yyyy-MM-dd): ");
            String input = scanner.nextLine().trim();

            try {
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException exception) {
                System.out.println("Invalid date format or invalid date! Please try again.");
            }
        }
    }

    public static String formatDate(Instant instant) {
        if (instant == null) {
            return "-";
        }
        return DATE_FORMATTER.format(instant);
    }

    public static String decimalFormat(BigDecimal amount) {
        Locale indonesia = new Locale("id", "ID");
        NumberFormat numberFormat = NumberFormat.getNumberInstance(indonesia);
        numberFormat.setMaximumFractionDigits(0);
        return numberFormat.format(amount);
    }

    public static int sizeInGB(int value) {
        return  (int) Math.ceil((double) value / 1024.0);
    }
}
