package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Category;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.City;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.repository.VenueRepository;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils.GeoEarthMathUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Pavel Asadchiy
 * on 12:47 24.03.18.
 */
@Slf4j
@Service
public class VenueService {

    private final GoogleService googleService;
    private final FoursquareService foursquareService;
    private final CategoryService categoryService;
    private final VenueRepository venueRepository;

    @Autowired
    public VenueService(GoogleService googleService, FoursquareService foursquareService, CategoryService categoryService, VenueRepository venueRepository) {
        this.googleService = googleService;
        this.foursquareService = foursquareService;
        this.categoryService = categoryService;
        this.venueRepository = venueRepository;
    }

    public VenueMiner sourceMiner(Source source) {
        switch (source) {
            case FOURSQUARE:
                return foursquareService;
            case GOOGLE:
                return googleService;
            default:
                throw new IllegalArgumentException("Incorrect data source type: " + source);
        }
    }

    @Transactional
    public List<Venue> quadTreeMine(City city, Source source, List<Category> categories) {
        VenueMiner venueMiner = sourceMiner(source);
        long startTime = System.currentTimeMillis();
        List<Venue> dirtyVenues = new ArrayList<>();
        Queue<BoundingBox> boxQueue = new ArrayDeque<>();
        boxQueue.add(city.getBoundingBox());
        int ind = 0;
        int apiCallCounter = 0;
        while (!boxQueue.isEmpty()) {
            log.debug("Trying to get places {} for boundingbox #{}...", categories, ind++);
            BoundingBox boundingBox = boxQueue.poll();
            Optional<List<Venue>> boundingBoxDirtyVenues = venueMiner.apiMine(boundingBox, categories);
            if (!boundingBoxDirtyVenues.isPresent()) {
                log.warn("API call failed, save search area and try repeat in future...");
//                SearchBoundingBox searchBoundingBox = new SearchBoundingBox();
//                searchBoundingBox.setBoundingBox(boundingBox);
//                searchBoundingBox.setSource(source);
//                searchBoundingBox.setCategories(categoryService.convertCategories(categories));
//                searchBoundingBoxRepository.saveAndFlush(searchBoundingBox);
                continue;
            }
            ++apiCallCounter;
            if (venueMiner.isReachTheLimit(boundingBoxDirtyVenues.get().size())) {
                log.debug("Split bounding box, because {} discovered max amount of venues", source);
                boxQueue.addAll(GeoEarthMathUtils.getQuarters(boundingBox));
                continue;
            }
            dirtyVenues.addAll(boundingBoxDirtyVenues.get());
            log.info("Was searched: {} {} dirty venues", boundingBoxDirtyVenues.get().size(), source);
        }
        log.info("API called approximately: {} times", apiCallCounter);
        List<Venue> validVenues = venueMiner.validate(city.getBoundingBox(), dirtyVenues).getValidVenues().stream()
                .peek(venue -> venue.setCity(city))
                .collect(Collectors.toList());
        log.info("Was searched {} dirty venues, after validation: {}", dirtyVenues.size(), validVenues.size());
        log.info("City area was scanned in {} ms", System.currentTimeMillis() - startTime);
        return venueRepository.save(validVenues);
    }

    @Transactional
    public List<Venue> quadTreeMineIfNeeded(City city, Source source, List<Category> categories) {
        List<String> strCategories = categoryService.convertCategories(categories);
        List<Venue> venues = new ArrayList<>();

        List<String> mineCategories = new ArrayList<>();
        for (String category : strCategories) {
            List<Venue> categoryVenues = venueRepository.findVenuesByCityAndCategory(city, category);
            if (CollectionUtils.isEmpty(categoryVenues)) {
                mineCategories.add(category);
            } else {
                venues.addAll(categoryVenues);
            }
        }

        if (CollectionUtils.isEmpty(mineCategories)) {
            return venues;
        }

        List<Venue> minedVenues = quadTreeMine(city, source, categoryService.valueOf(strCategories));
        venues.addAll(minedVenues);
        return venues;
    }
}
