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

    @Autowired
    public VenueService(GoogleService googleService) {
        this.googleService = googleService;
    }

    public AbstractVenueMiner getSourceMiner(Source source) {
        switch (source) {
            case FOURSQUARE:
            case GOOGLE:
                return googleService;
            default:
                throw new IllegalArgumentException("Incorrect data source type: " + source);
        }
    }

}
