package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain;

import lombok.Getter;
import lombok.Setter;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Category;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;

/**
 * Created by Pavel Asadchiy
 * on 13:24 24.03.18.
 */
@Getter
@Setter
public class Venue {

    private String title;
    private String description;
    private Category category;
    private Source source;
    private Marker location;
    private String address;
    private double rating;

}
