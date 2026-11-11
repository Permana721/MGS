package mana.game.shop.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtil {
    private static final Locale INDONESIA = Locale.forLanguageTag("id-ID");

    public static String decimalFormat(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }

        NumberFormat numberFormat = NumberFormat.getNumberInstance(INDONESIA);
        numberFormat.setMaximumFractionDigits(0);

        return numberFormat.format(amount);
    }
}
