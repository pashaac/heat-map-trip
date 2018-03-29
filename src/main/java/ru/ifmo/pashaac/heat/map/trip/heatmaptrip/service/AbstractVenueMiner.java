package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;


import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Category;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.City;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;

import java.util.List;

/**
 * Created by Pavel Asadchiy
 * on 15:57 17.02.18.
 */
public abstract class AbstractVenueMiner {

    public abstract List<Venue> mine(BoundingBox boundingBox, List<Category> categories);

    public abstract boolean isReachTheLimits(int venues);

    public abstract List<Venue> venueValidation(City city, BoundingBox venuesBoundingBox, List<Venue> venues);
}
