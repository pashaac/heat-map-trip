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
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service.VenueProxyService;

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

    private final VenueProxyService venueService;
    private final CategoryService categoryService;

    @Autowired
    public VenueController(VenueProxyService venueService, CategoryService categoryService) {
        this.venueService = venueService;
        this.categoryService = categoryService;
    }

    @RequestMapping(path = "/api/mine", method = RequestMethod.PUT)
    public VenuesBox getValidVenuesThroughClient(@RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
                                                 @RequestParam @ApiParam(value = "Venues category list", required = true, allowableValues = "Art, Nature") List<String> categories,
                                                 @RequestBody @ApiParam(value = "BoundingBox area of the search", required = true) BoundingBox boundingBox) {
        List<Venue> dirtyVenues = venueService.apiMine(boundingBox, categoryService.valueOf(categories), source);
        return venueService.validate(boundingBox, dirtyVenues, source);
    }

    @RequestMapping(value = "/categories", method = RequestMethod.GET)
    @ApiOperation(value = "Available categories")
    public List<String> getVenueCategories() {
        List<String> categories = categoryService.getVenueCategories();
        log.info("Venue categories: {}", categories);
        return categories;
    }

}
