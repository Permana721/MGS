package mana.game.shop.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class DateUtil {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(JAKARTA_ZONE);

    public static boolean isWithinLastDay(Instant instant, int days) {
        if (instant == null) return false;

        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
        return instant.isAfter(cutoff);
    }

    public static boolean isOlderThanDays(Instant instant, int days) {
        if (instant == null) return false;

        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
        return instant.isBefore(cutoff);
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
}
