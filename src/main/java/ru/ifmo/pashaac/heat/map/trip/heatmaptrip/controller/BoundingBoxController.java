package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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

    @RequestMapping(value = "/fail", method = RequestMethod.GET)
    public List<BoundingBox> getFailBoundingBoxes(@RequestParam @ApiParam(value = "City id of the boundingbox collection", required = true) Long cityId) {
        return boundingBoxService.getFailBoundingBoxes(cityId);
    }

    @RequestMapping(value = "/success", method = RequestMethod.GET)
    public List<BoundingBox> getSuccessBoundingBoxes(@RequestParam @ApiParam(value = "City id of the boundingbox collection", required = true) Long cityId) {
        return boundingBoxService.getSuccessBoundingBoxes(cityId);
    }

    @RequestMapping(value = "/grid", method = RequestMethod.PUT)
    @ApiOperation(value = "Create grid for boundingBox  area")
    public List<BoundingBox> gridBoundingBox(@RequestParam @ApiParam(value = "Grid row/col cells count", required = true) int grid,
                                             @RequestBody @ApiParam(value = "Covered boundingBox area", required = true) BoundingBox boundingBox) {
        if (grid < 1) {
            throw new IllegalArgumentException("Grid cells count in row or column should be more then zero");
        }
        return boundingBoxService.gridBoundingBox(boundingBox, grid);
    }
}
