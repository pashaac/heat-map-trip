package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by Pavel Asadchiy
 * on 13:37 24.03.18.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BoundingBox {

    private Marker southWest;
    private Marker northEast;

}
