package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.BoundingBox;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WeightedBoundingBox {
    private long id;
    private BoundingBox boundingBox;
    private double rating;
    private double count;
    private String color;
}
