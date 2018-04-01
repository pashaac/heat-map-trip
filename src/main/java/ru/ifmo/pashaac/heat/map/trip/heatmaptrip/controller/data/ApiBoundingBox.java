package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.controller.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;

import java.util.List;

/**
 * Created by Pavel Asadchiy
 * on 21:08 31.03.18.
 */
@Getter
@Setter
@Builder
public class ApiBoundingBox {

    BoundingBox boundingBox;
    List<BoundingBox> boundingBoxQuarters;
    List<Venue> venues;
    Boolean rateTheLimit;

}
