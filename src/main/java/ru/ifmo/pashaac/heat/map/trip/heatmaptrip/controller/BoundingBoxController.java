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

import java.util.List;
import java.util.Objects;

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
        return boundingBoxService.getValidBasedBoundingBoxes(cityId, source, categories, false);
    }

    @RequestMapping(value = "/valid", method = RequestMethod.GET)
    @ApiOperation(value = "Get valid - 'success data collection' bounding boxes from database")
    public List<BoundingBox> getSuccessBoundingBoxes(@RequestParam @ApiParam(value = "City id of the boundingbox collection", required = true) Long cityId,
                                                     @RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
                                                     @RequestParam @ApiParam(value = "Venues category list", required = true) List<String> categories) {
        return boundingBoxService.getValidBasedBoundingBoxes(cityId, source, categories, true);
    }

    @RequestMapping(value = "/grid", method = RequestMethod.GET)
    @ApiOperation(value = "Create grid for the city area")
    public List<BoundingBox> calculateGridCityBoundingBox(@RequestParam @ApiParam(value = "City id of the boundingbox collection", required = true) Long cityId,
                                                          @RequestParam @ApiParam(value = "Grid row/col cells count", required = true) Integer grid) {
        if (Objects.isNull(grid) || grid < 1) {
            throw new IllegalArgumentException("Grid cells count in row or column should be number which more then zero");
        }
        return boundingBoxService.gridBoundingBox(cityId, grid);
    }

    @RequestMapping(value = "/grid/heat/map", method = RequestMethod.PUT)
    @ApiOperation(value = "Create grid heat-map for the city area")
    public List<ClusterableBoundingBox> createHeatMapOnCityBoundingBoxGrid(@RequestParam @ApiParam(value = "City id of the boundingbox collection", required = true) Long cityId,
                                                                           @RequestParam @ApiParam(value = "Grid row/col cells count", required = true) Integer grid,
                                                                           @RequestBody @ApiParam(value = "Venues identifiers", required = true) List<Long> venueIds) {
        List<BoundingBox> gridBoundingBoxes = calculateGridCityBoundingBox(cityId, grid);
        List<Venue> venues = venueService.getVenues(venueIds);
        List<Venue> validVenues = venueService.venueValidation(venues);
        double pleasure = boundingBoxService.calculateAveragePleasure(validVenues);
        return boundingBoxService.smileClustering(venues, gridBoundingBoxes, pleasure);
    }

    @RequestMapping(path = "/mine", method = RequestMethod.PUT)
    public List<Venue> repeatMineBoundingBoxVenues(@RequestParam @ApiParam(value = "BoundingBox id of search area", required = true) Long boundingBoxId) {
        BoundingBox boundingBox = boundingBoxService.getBoundingBox(boundingBoxId);
        return venueService.quadTreeMine(boundingBox);
    }

}
