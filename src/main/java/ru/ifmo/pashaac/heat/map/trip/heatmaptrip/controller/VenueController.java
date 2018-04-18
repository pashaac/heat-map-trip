package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.controller;

import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.VenuesBox;
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
@RequestMapping(value = "/venue")
public class VenueController {

    private final VenueService venueService;

    @Autowired
    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @RequestMapping(path = "/api/call", method = RequestMethod.PUT)
    public VenuesBox getValidVenuesThroughClient(@RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
                                                 @RequestParam @ApiParam(value = "Venues category list", required = true, allowableValues = "Art, Nature") List<String> categories,
                                                 @RequestBody @ApiParam(value = "BoundingBox area of the search", required = true) BoundingBox boundingBox) {

        return venueService.apiMine(boundingBox, source, categories);
    }

    @RequestMapping(path = "/city/mine", method = RequestMethod.PUT)
    public List<Venue> getValidVenues(@RequestParam @ApiParam(value = "City id of the search", required = true) Long cityId,
                                      @RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
                                      @RequestParam @ApiParam(value = "Venues category list", required = true) List<String> categories) {
        return venueService.quadTreeMineIfNeeded(cityId, source, categories);
    }

//    @RequestMapping(value = "/venues/report", method = RequestMethod.GET)
//    @ApiOperation(value = "Generate Excel report for the city")
//    public String download(@RequestParam @ApiParam(value = "City id of the search", required = true) Long cityId,
//                           @RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
//                           @RequestParam /*@ApiParam(value = "Venues category list", required = true, allowableValues = "Art, Nature, Entertainment, Catering, Shrine, Municipality") */List<String> categories,
//                           Model model) {
//        model.addAttribute("venues", venueService.quadTreeMineIfNeeded(cityId, source, categories));
//        return "";
//    }
}
