package mana.game.shop.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Scanner;

public class InputUtil {
    private static final Scanner scanner = new Scanner(System.in);

    public static String stringInput(String info) {
        System.out.print(info);
        String input = scanner.nextLine();
        return input != null ? input.trim() : "";
    }

    public static int intInput(String info) {
        while (true) {
            System.out.print(info);
            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                ColorUtil.printError("Invalid input! Please enter a valid number.");
            }
        }
    }

    public static double doubleInput(String info) {
        System.out.print(info);
        double value = scanner.nextDouble();
        scanner.nextLine();
        return value;
    }

    public static BigDecimal bigDecimalInput(String info) {
        System.out.print(info);
        BigDecimal value = BigDecimal.valueOf(scanner.nextDouble());
        scanner.nextLine();
        return value;
    }

    public static String decimalFormat(BigDecimal amount) {
        Locale indonesia = Locale.forLanguageTag("id-ID");

        NumberFormat numberFormat = NumberFormat.getNumberInstance(indonesia);
        numberFormat.setMaximumFractionDigits(0);

        return numberFormat.format(amount);
    }

    public static int sizeInGB(int value) {
        return (int) Math.ceil((double) value / 1024.0);
    }
}
