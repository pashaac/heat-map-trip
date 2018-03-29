package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by Pavel Asadchiy
 * on 13:38 24.03.18.
 */
@Getter
public enum Category {

    PARK("", "park");

    private String foursquareKey;
    private String googleKey;

    Category(String foursquareKey, String googleKey) {
        this.foursquareKey = foursquareKey;
        this.googleKey = googleKey;
    }

    public static Optional<Category> valueOfGoogleCategory(String key) {
        return Arrays.stream(Category.values())
                .filter(category -> category.googleKey.contains(key))
                .findFirst();
    }


    public static List<Category> sights() {
        return Arrays.asList(PARK);
    }
}
