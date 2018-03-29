package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.BoundingBox;

/**
 * Created by Pavel Asadchiy
 * on 23:06 29.03.18.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class City {

    private String city;
    private String country;
    private BoundingBox boundingBox;


}
