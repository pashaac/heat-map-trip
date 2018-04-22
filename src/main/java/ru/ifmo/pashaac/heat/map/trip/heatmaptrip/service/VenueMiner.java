package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;


import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;

import java.util.List;
import java.util.Optional;

/**
 * Created by Pavel Asadchiy
 * on 15:57 17.02.18.
 */
public interface VenueMiner {

    List<Venue> apiCall(BoundingBox boundingBox);

    Optional<List<Venue>> apiMine(BoundingBox boundingBox);

    boolean isReachTheLimit(List<Venue> venues);

}
