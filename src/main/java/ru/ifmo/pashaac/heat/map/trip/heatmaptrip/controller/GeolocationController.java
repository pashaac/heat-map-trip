package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.City;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service.GeolocationService;

/**
 * Created by Pavel Asadchiy
 * on 23:04 29.03.18.
 */
@CrossOrigin(origins = "http://localhost:63342")
@RestController
@RequestMapping("/geolocation")
@Api(value = "Geolocation logic manager", description = "API to work with project geolocation resources")
public class GeolocationController {

    private final GeolocationService geolocationService;

    public GeolocationController(GeolocationService geolocationService) {
        this.geolocationService = geolocationService;
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ApiOperation(value = "Determine the Earth coordinates by location address")
    public Marker geolocation(@RequestParam @ApiParam(value = "Location address", required = true) String address) {
        return geolocationService.geolocation(address);
    }

    @RequestMapping(path = "/reverse", method = RequestMethod.PUT)
    @ApiOperation(value = "Determine the city according to coordinates")
    public City reverseGeolocation(@RequestParam @ApiParam(value = "Reverse geolocation latitude", required = true) double lat,
                                   @RequestParam @ApiParam(value = "Reverse geolocation longitude", required = true) double lng) {
        return geolocationService.reverseGeolocation(new Marker(lat, lng));
    }

}
