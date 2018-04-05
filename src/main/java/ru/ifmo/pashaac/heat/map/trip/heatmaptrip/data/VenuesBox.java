package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;

import java.util.List;

/**
 * Created by Pavel Asadchiy
 * on 11:26 05.04.18.
 */
@Getter
@Setter
@Builder
public class VenuesBox {

    private BoundingBox boundingBox;
    private List<BoundingBox> boundingBoxQuarters;

    private List<Venue> dirtyVenues;
    private List<Venue> validVenues;
    private boolean rateTheLimit;

}
