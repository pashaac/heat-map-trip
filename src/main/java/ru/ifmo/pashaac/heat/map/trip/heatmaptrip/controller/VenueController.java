package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.controller;

import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;
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
public class VenueController {

    private final VenueService venueService;

    @Autowired
    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @RequestMapping(path = "/dirty/api/call", method = RequestMethod.PUT)
    public List<Venue> getValidVenuesThroughClient(@RequestBody @ApiParam(value = "BoundingBox area of the search", required = true) BoundingBox boundingBox,
                                                   @RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
                                                   @RequestParam @ApiParam(value = "Venues category list", required = true) List<String> categories) {
        return venueService.apiMine(boundingBox, source, categories);
    }

    @RequestMapping(path = "/quad/tree/collect", method = RequestMethod.PUT)
    public List<Venue> getValidVenues(@RequestParam @ApiParam(value = "City id of the search", required = true) Long cityId,
                                      @RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
                                      @RequestParam @ApiParam(value = "Venues category list", required = true) List<String> categories) {
        List<Venue> dirtyVenues = venueService.quadTreeMineIfNeeded(cityId, source, categories);
        return venueService.venueValidation(dirtyVenues, categories);
    }

    @RequestMapping(path = "/quad/tree/collect/dirty", method = RequestMethod.PUT)
    public List<Venue> getDirtyVenues(@RequestParam @ApiParam(value = "City id of the search", required = true) Long cityId,
                                      @RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
                                      @RequestParam @ApiParam(value = "Venues category list", required = true) List<String> categories) {
        return venueService.quadTreeMineIfNeeded(cityId, source, categories);
    }

}
