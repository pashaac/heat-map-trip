package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Pavel Asadchiy
 * on 14:14 24.03.18.
 */
public class VenueUtils {

    public static String quotation(String text) {
        String noExtraSpacesText = text.replaceAll("\\s+", " ").trim();

        Pattern doubleQuotes = Pattern.compile("^(.*)\"(.*)\"(.*)$");
        Pattern singleQuotes = Pattern.compile("^(.*)'(.*)'(.*)$");

        return quotesReplacer(singleQuotes, quotesReplacer(doubleQuotes, noExtraSpacesText));
    }

    private static String quotesReplacer(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            text = matcher.replaceFirst("$1«$2»$3");
        }
        return text;
    }
}
