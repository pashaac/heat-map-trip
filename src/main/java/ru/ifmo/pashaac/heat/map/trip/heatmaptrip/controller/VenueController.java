package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service.BoundingBoxService;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service.VenueService;

import java.util.List;

/**
 * Created by Pavel Asadchiy
 * on 12:46 24.03.18.
 */
@Slf4j
@CrossOrigin(origins = "http://localhost:63342")
@RestController
@RequestMapping(value = "/venues")
@Api(value = "Main Venues information interface", description = "Interface to get information about venues and communicate with collection logic + another resources")
public class VenueController {

    private final VenueService venueService;
    private final BoundingBoxService boundingBoxService;

    @Autowired
    public VenueController(VenueService venueService, BoundingBoxService boundingBoxService) {
        this.venueService = venueService;
        this.boundingBoxService = boundingBoxService;
    }

    @RequestMapping(path = "/client/api/call", method = RequestMethod.PUT)
    public List<Venue> getVenuesThroughClient(@RequestBody @ApiParam(value = "BoundingBox search area", required = true) BoundingBox boundingBox,
                                              @RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
                                              @RequestParam @ApiParam(value = "Venues category list", required = true) List<String> categories) {
        return venueService.apiMine(boundingBox, source, categories);
    }

    @RequestMapping(path = "/collection", method = RequestMethod.PUT)
    public List<Venue> collectVenues(@RequestParam @ApiParam(value = "City id of the search", required = true) Long cityId,
                                     @RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
                                     @RequestParam @ApiParam(value = "Venues category list", required = true) List<String> categories) {
        return venueService.quadTreeMineIfNeeded(cityId, source, categories);
    }

    @RequestMapping(path = "/validation", method = RequestMethod.PUT)
    public List<Venue> validateVenues(@RequestBody @ApiParam(value = "Venue identifiers to validate", required = true) List<Long> venueIds) {
        List<Venue> venues = venueService.getVenues(venueIds);
        return venueService.venueValidation(venues);
    }

    @RequestMapping(path = "/distribution", method = RequestMethod.GET)
    public List<Marker> createVenuesDistribution(@RequestBody @ApiParam(value = "Venue identifiers to validate", required = true) List<Long> venueIds) {
        List<Venue> venues = venueService.getVenues(venueIds);
        List<Marker> markers = venueService.calculateVenuesDistribution(venues);
        log.info("Was generated {} markers", markers.size());
        return markers;
    }

    @RequestMapping(path = "/quad/tree/collect/dirty", method = RequestMethod.PUT)
    public List<Venue> getDirtyVenues(@RequestParam @ApiParam(value = "City id of the search", required = true) Long cityId,
                                      @RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
                                      @RequestParam @ApiParam(value = "Venues category list", required = true) List<String> categories) {
        return venueService.quadTreeMineIfNeeded(cityId, source, categories);
    }

}
