package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.controller;

import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.controller.data.ApiBoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Category;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service.AbstractVenueMiner;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service.VenueService;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils.GeoEarthMathUtils;

import java.util.List;

/**
 * Created by Pavel Asadchiy
 * on 12:46 24.03.18.
 */
@CrossOrigin(origins = "http://localhost:63342")
@RestController
@RequestMapping(value = "/venue")
public class VenueController {

    private final VenueService venueService;

    @Autowired
    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @RequestMapping(path = "/api/call", method = RequestMethod.GET)
    public List<Venue> getVenuesThroughClient(@RequestParam @ApiParam(value = "Latitude coordinate of the search", required = true) double latitude,
                                              @RequestParam @ApiParam(value = "Longitude coordinate of the search", required = true) double longitude,
                                              @RequestParam @ApiParam(value = "Search radius", required = true) int radius,
                                              @RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source) {
        AbstractVenueMiner venueMiner = venueService.getSourceMiner(source);
        return venueMiner.apiCall(new Marker(latitude, longitude), radius, Category.sights());
    }


    @RequestMapping(path = "/api/call/details", method = RequestMethod.PUT)
    public ApiBoundingBox getFullVenuesThroughClient(@RequestBody @ApiParam(value = "BoundingBox of the search", required = true) BoundingBox boundingBox
                                                     /*@RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source*/) {
        AbstractVenueMiner venueMiner = venueService.getSourceMiner(Source.GOOGLE);
        List<Venue> dirtyVenues = venueMiner.mine(boundingBox, Category.sights());
        List<Venue> venues = venueMiner.venueValidation(boundingBox, dirtyVenues);
        return ApiBoundingBox.builder()
                .boundingBox(boundingBox)
                .venues(venues)
                .rateTheLimit(venueMiner.isReachTheLimit(dirtyVenues.size()))
                .boundingBoxQuarters(GeoEarthMathUtils.getQuarters(boundingBox))
                .build();
    }

}
