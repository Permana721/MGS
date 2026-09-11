package mana.game.shop.util;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Scanner;

public class InputUtil {
    private static Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String stringInput(String info) {
        System.out.print(info + " ");
        return scanner.nextLine();
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

    public static LocalDate dateInput(String info) {
        while (true) {
            System.out.print(info + " (format: yyyy-MM-dd): ");
            String input = scanner.nextLine().trim();

            try {
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException exception) {
                System.out.println("Format tanggal salah atau tanggal tidak valid! Silakan coba lagi.");
            }
        }
    }

    public static String decimalFormat(double amount) {
        Locale indonesia = new Locale("id", "ID");
        NumberFormat numberFormat = NumberFormat.getNumberInstance(indonesia);
        numberFormat.setMaximumFractionDigits(0);
        return numberFormat.format(amount);
    }
}
