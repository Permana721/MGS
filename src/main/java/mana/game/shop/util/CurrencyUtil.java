package mana.game.shop.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyUtil {
    private static final DecimalFormat NUMBER_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        NUMBER_FORMAT = new DecimalFormat("#,##0", symbols);
    }

    public static String toRupiahNumber(double amount) {
        return NUMBER_FORMAT.format(amount);
    }
}
