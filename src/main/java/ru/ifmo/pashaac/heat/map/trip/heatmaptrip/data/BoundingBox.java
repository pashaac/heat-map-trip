package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Pavel Asadchiy
 * on 13:37 24.03.18.
 */
@Getter
@Setter
public class BoundingBox {

    private Marker southWest;
    private Marker northEast;

}
