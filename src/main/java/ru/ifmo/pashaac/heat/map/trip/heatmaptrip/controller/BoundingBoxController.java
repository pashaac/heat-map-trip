package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.ClusterableBoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service.BoundingBoxService;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service.VenueService;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils.GeoEarthMathUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin(origins = "http://localhost:63342")
@RestController
@RequestMapping(value = "/boundingboxes")
@Api(value = "Bounding box logic manager", description = "API to work with project bounding box entities")
public class BoundingBoxController {

    private final BoundingBoxService boundingBoxService;
    private final VenueService venueService;

    @Autowired
    public BoundingBoxController(BoundingBoxService boundingBoxService, VenueService venueService) {
        this.boundingBoxService = boundingBoxService;
        this.venueService = venueService;
    }

    @RequestMapping(value = "/invalid", method = RequestMethod.GET)
    @ApiOperation(value = "Get invalid - 'fail data collection' bounding boxes from database")
    public List<BoundingBox> getInvalidBoundingBoxes(@RequestParam @ApiParam(value = "City id of the boundingbox collection", required = true) Long cityId,
                                                     @RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
                                                     @RequestParam @ApiParam(value = "Venues category list", required = true) List<String> categories) {
        List<BoundingBox> boundingBoxes = boundingBoxService.getBoundingBoxes(cityId, source, categories, false);
        return boundingBoxService.getUniqueBoundingBox(boundingBoxes);
    }

    @RequestMapping(value = "/valid", method = RequestMethod.GET)
    @ApiOperation(value = "Get valid - 'success data collection' bounding boxes from database")
    public List<BoundingBox> getSuccessBoundingBoxes(@RequestParam @ApiParam(value = "City id of the boundingbox collection", required = true) Long cityId,
                                                     @RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
                                                     @RequestParam @ApiParam(value = "Venues category list", required = true) List<String> categories) {
        List<BoundingBox> boundingBoxes = boundingBoxService.getBoundingBoxes(cityId, source, categories, true);
        return boundingBoxService.getUniqueBoundingBox(boundingBoxes);
    }

    @RequestMapping(value = "/grid", method = RequestMethod.GET)
    @ApiOperation(value = "Create grid for the city area")
    public List<BoundingBox> calculateGridCityBoundingBox(@RequestParam @ApiParam(value = "City id of the boundingbox collection", required = true) Long cityId,
                                                          @RequestParam @ApiParam(value = "Grid row/col cells count", required = true) Integer grid) {
        if (Objects.isNull(grid) || grid < 1) {
            throw new IllegalArgumentException("Grid cells count in row or column should be number which more then zero");
        }
        List<BoundingBox> boundingBoxes = boundingBoxService.gridBoundingBox(cityId, grid);
        double width = GeoEarthMathUtils.distance(boundingBoxes.get(0).getSouthWest(), GeoEarthMathUtils.getSouthEast(boundingBoxes.get(0)));
        double height = GeoEarthMathUtils.distance(boundingBoxes.get(0).getSouthWest(), GeoEarthMathUtils.getNorthWest(boundingBoxes.get(0)));
        log.info("Approximately cell size: {} x {}", width, height);
        return boundingBoxes;
    }

    @RequestMapping(value = "/grid/collection", method = RequestMethod.GET)
    @ApiOperation(value = "Collect venues through the grid areas")
    public List<Venue> collectGridCityVenues(@RequestParam @ApiParam(value = "City id of the boundingbox collection", required = true) Long cityId,
                                             @RequestParam @ApiParam(value = "Grid row/col cells count", required = true) Integer grid,
                                             @RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
                                             @RequestParam @ApiParam(value = "Venues category list", required = true) List<String> categories) {
        List<BoundingBox> boundingBoxes = calculateGridCityBoundingBox(cityId, grid);
        List<Venue> venues = boundingBoxes.stream()
                .flatMap(boundingBox -> venueService.apiMine(boundingBox, source, categories).stream())
                .collect(Collectors.toList());
        log.info("Was found {} venues with positive rating from categories: {}", venues.size(), categories);
        return venues;
    }

    @RequestMapping(value = "/grid/heat/map", method = RequestMethod.PUT)
    @ApiOperation(value = "Create grid heat-map for the city area")
    public List<ClusterableBoundingBox> createHeatMapOnCityBoundingBoxGrid(@RequestParam @ApiParam(value = "City id of the boundingbox collection", required = true) Long cityId,
                                                                           @RequestParam @ApiParam(value = "Grid row/col cells count", required = true) Integer grid,
                                                                           @RequestBody @ApiParam(value = "Venues identifiers", required = true) List<Long> venueIds) {
        List<BoundingBox> gridBoundingBoxes = calculateGridCityBoundingBox(cityId, grid);
        List<Venue> venues = venueService.getVenues(venueIds);
        List<Venue> validVenues = venueService.venueValidation(venues);
//        double pleasure = boundingBoxService.calculateAveragePleasure(validVenues);
        return boundingBoxService.smileClustering(validVenues, gridBoundingBoxes, 0);
    }

    @RequestMapping(path = "/mine", method = RequestMethod.PUT)
    public List<Venue> repeatMineBoundingBoxVenues(@RequestParam @ApiParam(value = "BoundingBox id of search area", required = true) Long boundingBoxId) {
        BoundingBox boundingBox = boundingBoxService.getBoundingBox(boundingBoxId);
        boundingBox.setValid(false);
        return venueService.quadTreeMine(Collections.singletonList(boundingBox));
    }

}
