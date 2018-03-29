package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.controller;

import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service.VenueService;

import java.util.List;

/**
 * Created by Pavel Asadchiy
 * on 12:46 24.03.18.
 */
@RestController
@RequestMapping(value = "/venue")
public class VenueController {

    private final VenueService venueService;

    @Autowired
    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @CrossOrigin(origins = "http://localhost:63342")
    @GetMapping("/api/call")
    public List<Venue> getVenuesThroughClient(@RequestParam @ApiParam(value = "Latitude coordinate of the search", required = true) double latitude,
                                              @RequestParam @ApiParam(value = "Longitude coordinate of the search", required = true) double longitude,
                                              @RequestParam @ApiParam(value = "Search radius", required = true) int radius,
                                              @RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source) {
        return venueService.apiCallThroughClient(new Marker(latitude, longitude), radius, source);
    }

}
