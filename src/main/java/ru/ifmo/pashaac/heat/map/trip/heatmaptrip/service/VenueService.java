package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Category;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;

import java.util.List;

/**
 * Created by Pavel Asadchiy
 * on 12:47 24.03.18.
 */
@Service
public class VenueService {

    private final GoogleService googleService;

    @Autowired
    public VenueService(GoogleService googleService) {
        this.googleService = googleService;
    }

    public List<Venue> apiCallThroughClient(Marker marker, int radius, Source source) {
        switch (source) {
            case FOURSQUARE:
            case GOOGLE:
                return googleService.apiCall(marker, radius, Category.sights());
            default:
                throw new IllegalArgumentException("Incorrect data source type: " + source);
        }
    }
}
