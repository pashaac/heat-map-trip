package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Category;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.VenuesBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;

import java.util.List;

/**
 * Created by Pavel Asadchiy
 * on 12:47 24.03.18.
 */
@Service
public class VenueProxyService {

    private final GoogleService googleService;
    private final FoursquareService foursquareService;

    @Autowired
    public VenueProxyService(GoogleService googleService, FoursquareService foursquareService) {
        this.googleService = googleService;
        this.foursquareService = foursquareService;
    }

    public VenueMiner getMinerBySource(Source source) {
        switch (source) {
            case FOURSQUARE:
                return foursquareService;
            case GOOGLE:
                return googleService;
            default:
                throw new IllegalArgumentException("Incorrect data source type: " + source);
        }
    }


    public List<Venue> apiMine(BoundingBox boundingBox, List<Category> categories, Source source) {
        VenueMiner miner = getMinerBySource(source);
        return miner.apiMine(boundingBox, categories);
    }

    public VenuesBox validate(BoundingBox venuesBoundingBox, List<Venue> dirtyVenues, Source source) {
        VenueMiner miner = getMinerBySource(source);
        return miner.validate(venuesBoundingBox, dirtyVenues);
    }

}
