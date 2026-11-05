package mana.game.shop.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyUtil {
    public static String decimalFormat(BigDecimal amount) {
        Locale indonesia = Locale.forLanguageTag("id-ID");

        NumberFormat numberFormat = NumberFormat.getNumberInstance(indonesia);
        numberFormat.setMaximumFractionDigits(0);

        return numberFormat.format(amount);
    }
}
