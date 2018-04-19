package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.City;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.repository.CityRepository;

import java.util.*;
import java.util.stream.Collectors;

import static ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source.FOURSQUARE;

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
    private final CityRepository cityRepository;
    private final BoundingBoxService boundingBoxService;
    private final VenueTransactionalService venueTransactionalService;

    @Autowired
    public VenueService(GoogleService googleService, FoursquareService foursquareService, CategoryService categoryService, CityRepository cityRepository, BoundingBoxService boundingBoxService, VenueTransactionalService venueTransactionalService) {
        this.googleService = googleService;
        this.foursquareService = foursquareService;
        this.categoryService = categoryService;
        this.cityRepository = cityRepository;
        this.boundingBoxService = boundingBoxService;
        this.venueTransactionalService = venueTransactionalService;
    }

    public List<Venue> apiMine(BoundingBox boundingBox, Source source, List<String> categories) {
        VenueMiner venueMiner;
        switch (source) {
            case FOURSQUARE:
                venueMiner = foursquareService;
                boundingBox.setSource(FOURSQUARE);
                boundingBox.setCategories(categoryService.join(categories));
                break;
            case GOOGLE:
                venueMiner = googleService;
                boundingBox.setSource(Source.GOOGLE);
                boundingBox.setCategories(categoryService.join(categories));
                break;
            default:
                throw new IllegalArgumentException("Incorrect data source type: " + source);
        }
        return venueMiner.apiMine(boundingBox).orElse(Collections.emptyList());
    }

    private List<Venue> dirtyVenuesQuadTreeMine(City city, Source source, List<String> categories) {
        BoundingBox cityBoundingBox = city.getBoundingBox();
        switch (source) {
            case FOURSQUARE:
                cityBoundingBox.setSource(FOURSQUARE);
                cityBoundingBox.setCategories(categoryService.join(categories));
                break;
            case GOOGLE:
                cityBoundingBox.setSource(Source.GOOGLE);
                cityBoundingBox.setCategories(categoryService.join(categories));
                break;
            default:
                throw new IllegalArgumentException("Incorrect data source type: " + source);
        }
        BoundingBox savedBoundingBox = venueTransactionalService.save(cityBoundingBox);
        log.info("Mining places for city {} by categories {} starting...", city.getCity(), categories);
        return dirtyVenuesQuadTreeMine(savedBoundingBox);
    }

    public List<Venue> dirtyVenuesQuadTreeMine(BoundingBox boundingBox) {
        if (StringUtils.isEmpty(boundingBox.getSource())) {
            log.info("Bounding box with empty search key, skip it");
            return Collections.emptyList();
        }

        long startTime = System.currentTimeMillis();

        VenueMiner venueMiner;
        switch (boundingBox.getSource()) {
            case FOURSQUARE:
                venueMiner = foursquareService;
                break;
            case GOOGLE:
                venueMiner = googleService;
                break;
            default:
                throw new IllegalArgumentException("Incorrect data source type: " + boundingBox.getSource());
        }

        Queue<BoundingBox> boxQueue = new ArrayDeque<>(Collections.singleton(boundingBox));
        int ind = 0;
        int apiCallCounter = 0;
        List<Venue> dirtyVenues = new ArrayList<>();
        while (!boxQueue.isEmpty()) {
            log.debug("Trying to get places for boundingBox #{}...", ind++);
            boundingBox = Optional.ofNullable(boxQueue.poll())
                    .orElseThrow(() -> new IllegalArgumentException("Null boundingBox during mining process"));
            Optional<List<Venue>> boundingBoxDirtyVenues = venueMiner.apiMine(boundingBox);
            ++apiCallCounter;
            if (!boundingBoxDirtyVenues.isPresent()) {
                log.warn("API call failed... Search area already saved... Scheduler try repeat search in future...");
                continue;
            }
            if (venueMiner.isReachTheLimit(boundingBoxDirtyVenues.get())) {
                log.debug("Split bounding box, because {} discovered max amount of venues", boundingBox.getSource());
                List<BoundingBox> quarters = venueTransactionalService.splitBoundingBox(boundingBox);
                boxQueue.addAll(quarters);
                continue;
            }
            boundingBox.setVenues(boundingBoxDirtyVenues.get());
            List<Venue> savedVenues = venueTransactionalService.saveVenueBoundingBox(boundingBox).getVenues();
            dirtyVenues.addAll(savedVenues);
            log.info("Was searched: {} {} dirty venues", boundingBoxDirtyVenues.get().size(), boundingBox.getSource());
        }
        log.info("API called approximately: {} times", apiCallCounter);
        log.info("Was searched {} dirty venues", dirtyVenues.size());
        log.info("City area was scanned in {} ms", System.currentTimeMillis() - startTime);
        return dirtyVenues;
    }


    public List<Venue> quadTreeMineIfNeeded(Long cityId, Source source, List<String> categories) {
        List<BoundingBox> invalidBoundingBoxes = boundingBoxService.getInvalidBoundingBoxes(cityId, source, categories);
        if (!CollectionUtils.isEmpty(invalidBoundingBoxes)) {
            throw new RuntimeException("Service contains some amount invalid bounding boxes, repeat your request in a few minutes");
        }

        List<Venue> dirtyVenues = new ArrayList<>();
        List<String> notFoundCategories = new ArrayList<>();
        for (String category : categories) {
            List<BoundingBox> categoryBoundingBoxes = boundingBoxService.getValidBoundingBoxes(cityId, source, category);
            if (CollectionUtils.isEmpty(categoryBoundingBoxes)) {
                notFoundCategories.add(category);
                continue;
            }
            List<Venue> categoryDirtyVenues = categoryBoundingBoxes.stream()
                    .map(BoundingBox::getVenues)
                    .flatMap(Collection::stream)
                    .filter(venue -> category.equals(venue.getCategory()))
                    .collect(Collectors.toList());
            dirtyVenues.addAll(categoryDirtyVenues);
        }

//        List<Category> categories = categoryService.map(strCategories);
//
//        Map<Category, List<Venue>> cityCategoryToVenuesMap = categories.stream()
//                .collect(Collectors.toMap(category -> category, category -> city.getBoundingBoxes().stream()
//                        .flatMap(boundingBox -> boundingBox.getVenues().stream())
//                        .filter(venue -> category.getTitle().equals(venue.getCategory()))
//                        .collect(Collectors.toList())));
//
//        List<String> categoriesToMine = cityCategoryToVenuesMap.entrySet().stream()
//                .filter(entry -> CollectionUtils.isEmpty(entry.getValue()))
//                .map(Map.Entry::getKey)
//                .peek(category -> log.info("No venues in city = {} by source = {}, category = {}", city.getCity(), source, category.getTitle()))
//                .map(Category::getTitle)
//                .collect(Collectors.toList());
//
//        List<Venue> cityVenues = cityCategoryToVenuesMap.entrySet().stream()
//                .filter(entry -> !CollectionUtils.isEmpty(entry.getValue()))
//                .map(Map.Entry::getValue)
//                .peek(venues -> log.info("Was found {} venues in city = {} by source = {}, category = {}", city.getCity(), source, venues.size()))
//                .flatMap(Collection::stream)
//                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(notFoundCategories)) {
            return dirtyVenues;
        }
        List<Venue> minedDirtyVenues = dirtyVenuesQuadTreeMine(cityRepository.findOne(cityId), source, notFoundCategories);
        dirtyVenues.addAll(minedDirtyVenues);
        return dirtyVenues;
    }

}
