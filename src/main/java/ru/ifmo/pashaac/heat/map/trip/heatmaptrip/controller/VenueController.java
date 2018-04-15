package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.VenuesBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service.CategoryService;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service.VenueService;

import java.util.Collections;
import java.util.List;

/**
 * Created by Pavel Asadchiy
 * on 12:46 24.03.18.
 */
@Slf4j
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

    @RequestMapping(path = "/api/call", method = RequestMethod.PUT)
    public VenuesBox getValidVenuesThroughClient(@RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
                                                 @RequestParam @ApiParam(value = "Venues category list", required = true, allowableValues = "Art, Nature") List<String> categories,
                                                 @RequestBody @ApiParam(value = "BoundingBox area of the search", required = true) BoundingBox boundingBox) {
        List<Venue> dirtyVenues = venueService.sourceMiner(source).apiMine(boundingBox, categoryService.valueOf(categories))
                .orElse(Collections.emptyList());
        return venueService.sourceMiner(source).validate(boundingBox, dirtyVenues);
    }

    @RequestMapping(path = "/city/mine", method = RequestMethod.PUT)
    public List<Venue> getValidVenues(@RequestParam @ApiParam(value = "City id of the search", required = true) Long cityId,
                                      @RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
                                      @RequestParam @ApiParam(value = "Venues category list", required = true, allowableValues = "Art, Nature, Entertainment, Catering, Shrine, Municipality") List<String> categories) {
        return venueService.quadTreeMineIfNeeded(cityId, source, categoryService.valueOf(categories));
    }

    @RequestMapping(value = "/categories", method = RequestMethod.GET)
    @ApiOperation(value = "Available categories")
    public List<String> getVenueCategories() {
        return categoryService.getVenueCategories();
    }

}
