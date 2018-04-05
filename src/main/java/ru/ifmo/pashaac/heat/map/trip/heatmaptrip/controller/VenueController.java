package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.controller;

import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.VenuesBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service.CategoryService;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service.VenueMiner;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service.VenueService;

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
    private final CategoryService categoryService;

    @Autowired
    public VenueController(VenueService venueService, CategoryService categoryService) {
        this.venueService = venueService;
        this.categoryService = categoryService;
    }

    @RequestMapping(path = "/api/call/dirty", method = RequestMethod.PUT)
    public List<Venue> getDirtyVenuesThroughClient(@RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
                                                   @RequestParam @ApiParam(value = "Venues category list", required = true, allowableValues = "Art, Nature") List<String> categories,
                                                   @RequestBody @ApiParam(value = "BoundingBox area of the search", required = true) BoundingBox boundingBox) {
        VenueMiner venueMiner = venueService.getMinerBySource(source);
        return venueMiner.apiCall(boundingBox, categoryService.valueOf(categories));
    }


    @RequestMapping(path = "/api/call/valid", method = RequestMethod.PUT)
    public VenuesBox getValidVenuesThroughClient(@RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
                                                 @RequestParam @ApiParam(value = "Venues category list", required = true, allowableValues = "Art, Nature") List<String> categories,
                                                 @RequestBody @ApiParam(value = "BoundingBox area of the search", required = true) BoundingBox boundingBox) {
        VenueMiner venueMiner = venueService.getMinerBySource(source);
        List<Venue> dirtyVenues = getDirtyVenuesThroughClient(source, categories, boundingBox);
        return venueMiner.validate(boundingBox, dirtyVenues);
    }

}
