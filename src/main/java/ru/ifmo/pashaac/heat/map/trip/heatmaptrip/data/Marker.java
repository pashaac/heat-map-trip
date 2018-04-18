package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;

/**
 * Created by Pavel Asadchiy
 * on 13:34 24.03.18.
 */
@Getter
@Setter
@NoArgsConstructor

@Embeddable
public class Marker {

    private double latitude;
    private double longitude;

    public Marker(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
