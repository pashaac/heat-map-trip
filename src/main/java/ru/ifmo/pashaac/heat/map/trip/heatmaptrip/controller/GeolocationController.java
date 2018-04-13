package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.City;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service.GeolocationService;

import java.util.List;

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

    @RequestMapping(value = "/grid/boundingbox", method = RequestMethod.PUT)
    @ApiOperation(value = "Create grid for boundingBox  area")
    public List<BoundingBox> gridBoundingBox(@RequestParam @ApiParam(value = "Grid row/col cells count", required = true) int grid,
                                             @RequestBody @ApiParam(value = "Covered boundingBox area", required = true) BoundingBox boundingBox) {
        if (grid < 1) {
            throw new IllegalArgumentException("Grid cells count in row or column should be more then zero");
        }
        return geolocationService.gridBoundingBox(boundingBox, grid);
    }
}
