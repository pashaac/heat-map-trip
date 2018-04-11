package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;


import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Category;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.VenuesBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils.GeoEarthMathUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Pavel Asadchiy
 * on 15:57 17.02.18.
 */
public interface VenueMiner {

    List<Venue> apiCall(BoundingBox boundingBox, List<Category> categories);

    Optional<List<Venue>> apiMine(BoundingBox boundingBox, List<Category> categories);

    boolean isReachTheLimit(int venues);

    default VenuesBox validate(BoundingBox venuesBoundingBox, List<Venue> dirtyVenues) {
        double average = dirtyVenues.stream().mapToDouble(Venue::getRating).average().orElse(0.0);
        List<Venue> validVenues = dirtyVenues.stream()
                .filter(venue -> Objects.nonNull(venue.getCategory()))
                .filter(venue -> Character.isUpperCase(venue.getTitle().charAt(0)))
                .filter(venue -> venue.getRating() > average * 0.1) // more than 10% of average rating
                .filter(venue -> GeoEarthMathUtils.contains(venuesBoundingBox, venue.getLocation()))
                .collect(Collectors.toList());
        return VenuesBox.builder()
                .boundingBox(venuesBoundingBox)
                .boundingBoxQuarters(GeoEarthMathUtils.getQuarters(venuesBoundingBox))
                .dirtyVenues(dirtyVenues)
                .validVenues(validVenues)
                .rateTheLimit(isReachTheLimit(dirtyVenues.size()))
                .build();
    }

}
