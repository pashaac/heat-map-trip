package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;

/**
 * Created by Pavel Asadchiy
 * on 12:47 24.03.18.
 */
@Service
public class VenueService {

    private final GoogleService googleService;
    private final FoursquareService foursquareService;

    @Autowired
    public VenueService(GoogleService googleService, FoursquareService foursquareService) {
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

}
