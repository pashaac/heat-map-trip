package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service.BoundingBoxService;

import java.util.List;

@Slf4j
@CrossOrigin(origins = "http://localhost:63342")
@RestController
@RequestMapping(value = "/boundingboxes")
public class BoundingBoxController {

    private final BoundingBoxService boundingBoxService;

    @Autowired
    public BoundingBoxController(BoundingBoxService boundingBoxService) {
        this.boundingBoxService = boundingBoxService;
    }

    @RequestMapping(value = "/invalid", method = RequestMethod.GET)
    public List<BoundingBox> getInvalidBoundingBoxes(@RequestParam @ApiParam(value = "City id of the boundingbox collection", required = true) Long cityId,
                                                     @RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
                                                     @RequestParam @ApiParam(value = "Venues category list", required = true) List<String> categories) {
        return boundingBoxService.getInvalidBoundingBoxes(cityId, source, categories);
    }

    @RequestMapping(value = "/valid", method = RequestMethod.GET)
    public List<BoundingBox> getSuccessBoundingBoxes(@RequestParam @ApiParam(value = "City id of the boundingbox collection", required = true) Long cityId,
                                                     @RequestParam @ApiParam(value = "Venues data source", required = true, allowableValues = "FOURSQUARE, GOOGLE") Source source,
                                                     @RequestParam @ApiParam(value = "Venues category list", required = true) List<String> categories) {
        return boundingBoxService.getValidBoundingBoxes(cityId, source, categories);
    }

    @RequestMapping(value = "/grid", method = RequestMethod.GET)
    @ApiOperation(value = "Create grid for boundingBox  area")
    public List<BoundingBox> gridBoundingBox(@RequestParam @ApiParam(value = "City id of the boundingbox collection", required = true) Long cityId,
                                             @RequestParam @ApiParam(value = "Grid row/col cells count", required = true) int grid) {
        if (grid < 1) {
            throw new IllegalArgumentException("Grid cells count in row or column should be more then zero");
        }
        return boundingBoxService.gridBoundingBox(cityId, grid);
    }
}
