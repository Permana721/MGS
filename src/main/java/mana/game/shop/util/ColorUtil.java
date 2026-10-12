package mana.game.shop.util;

public class ColorUtil {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String CYAN = "\u001B[36m";

    public static void printError(String message) {
        System.out.println(RED + "[!] " + message + RESET);
    }

    public static void printSuccess(String message) {
        System.out.println(GREEN + "[✓] " + message + RESET);
    }
}
