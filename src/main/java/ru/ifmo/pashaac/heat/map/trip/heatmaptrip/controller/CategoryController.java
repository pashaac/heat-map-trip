package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service.CategoryService;

import java.util.List;

@Slf4j
@CrossOrigin(origins = "http://localhost:63342")
@RestController
@RequestMapping(value = "/categories")
@Api(value = "Interface to communicate with categories", description = "API to work with project categories based logic")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "Heat-Map-Trip service available categories")
    public List<String> getVenueCategories() {
        return categoryService.getTotalCategories();
    }

}
