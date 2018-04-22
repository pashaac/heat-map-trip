package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.configuration.properties.VenueCategoryConfigurationProperties;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.City;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.repository.CityRepository;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils.GeoEarthMathUtils;

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
    private final VenueCategoryConfigurationProperties venueCategoryConfigurationProperties;
    private final VenueTransactionalService venueTransactionalService;

    @Autowired
    public VenueService(GoogleService googleService, FoursquareService foursquareService, CategoryService categoryService, CityRepository cityRepository, BoundingBoxService boundingBoxService, VenueCategoryConfigurationProperties venueCategoryConfigurationProperties, VenueTransactionalService venueTransactionalService) {
        this.googleService = googleService;
        this.foursquareService = foursquareService;
        this.categoryService = categoryService;
        this.cityRepository = cityRepository;
        this.boundingBoxService = boundingBoxService;
        this.venueCategoryConfigurationProperties = venueCategoryConfigurationProperties;
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

    public List<Venue> dirtyVenuesQuadTreeMine(BoundingBox rootBoundingBox) {
        if (StringUtils.isEmpty(rootBoundingBox.getSource())) {
            log.info("Bounding box with empty search key, skip it");
            return Collections.emptyList();
        }

        long startTime = System.currentTimeMillis();

        VenueMiner venueMiner;
        switch (rootBoundingBox.getSource()) {
            case FOURSQUARE:
                venueMiner = foursquareService;
                break;
            case GOOGLE:
                venueMiner = googleService;
                break;
            default:
                throw new IllegalArgumentException("Incorrect data source type: " + rootBoundingBox.getSource());
        }

        Queue<BoundingBox> boxQueue = new ArrayDeque<>(Collections.singleton(rootBoundingBox));
        int ind = 0;
        int apiCallCounter = 0;
        List<Venue> dirtyVenues = new ArrayList<>();
        while (!boxQueue.isEmpty()) {
            log.debug("Trying to get places for boundingBox #{}...", ind++);
            BoundingBox boundingBox = Optional.ofNullable(boxQueue.poll())
                    .orElseThrow(() -> new IllegalArgumentException("Null boundingBox during mining process"));
            Optional<List<Venue>> minedBboxDirtyVenues = venueMiner.apiMine(boundingBox);
            ++apiCallCounter;
            if (!minedBboxDirtyVenues.isPresent()) {
                log.warn("API call failed... Search area already saved... Scheduler try repeat search in future...");
                continue;
            }
            if (venueMiner.isReachTheLimit(minedBboxDirtyVenues.get())) {
                log.debug("Split bounding box, because {} discovered max amount of venues", boundingBox.getSource());
                List<BoundingBox> quarters = venueTransactionalService.splitBoundingBox(boundingBox);
                boxQueue.addAll(quarters);
                continue;
            }
            List<Venue> bboxDirtyVenues = minedBboxDirtyVenues.get().stream()
                    .filter(venue -> GeoEarthMathUtils.contains(boundingBox, venue.getLocation()))
                    .collect(Collectors.toList());
            boundingBox.setVenues(bboxDirtyVenues);
            List<Venue> savedVenues = venueTransactionalService.saveVenueBoundingBox(boundingBox).getVenues();
            dirtyVenues.addAll(savedVenues);
            log.info("Was searched: {} {} dirty venues", bboxDirtyVenues.size(), boundingBox.getSource());
        }
        log.info("API called approximately: {} times", apiCallCounter);
        log.info("Was searched {} dirty venues", dirtyVenues.size());
        log.info("City area was scanned in {} ms", System.currentTimeMillis() - startTime);
        return dirtyVenues;
    }


    public List<Venue> quadTreeMineIfNeeded(Long cityId, Source source, List<String> categories) {
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
        if (CollectionUtils.isEmpty(notFoundCategories)) {
            return dirtyVenues;
        }
        List<Venue> minedDirtyVenues = dirtyVenuesQuadTreeMine(cityRepository.findOne(cityId), source, notFoundCategories);
        dirtyVenues.addAll(minedDirtyVenues);
        return dirtyVenues;
    }

    public List<Venue> venueValidation(List<Venue> dirtyVenues, List<String> categories) {
        // TODO: Use special filtering: invalidate all -> filtering -> set valid flag in db -> return
        // Possible remove flag game, but for debug very useful
        log.info("Plan to filter {} dirty venues from categories = {}", dirtyVenues.size(), categories);
        long startTime = System.currentTimeMillis();
        List<Venue> venues = dirtyVenues.stream()
                .peek(venue -> venue.setValid(false))
                .collect(Collectors.toList());
        venues = venueTransactionalService.saveVenues(venues); // invalidate all
        venues = venues.stream()
                .filter(venue -> Objects.nonNull(venue.getCategory()))
                .filter(venue -> Character.isUpperCase(venue.getTitle().charAt(0)))
                .collect(Collectors.toList());

        Map<String, List<Venue>> groupedVenues = venues.stream().collect(Collectors.groupingBy(Venue::getCategory));
        Map<String, Double> averageRating = venues.stream().collect(Collectors.groupingBy(Venue::getCategory, Collectors.averagingDouble(Venue::getRating)));
        venues = groupedVenues.entrySet().stream()
                .map(Map.Entry::getValue)
                .flatMap(subVenues -> subVenues.stream()
                        .filter(venue -> venue.getRating() > averageRating.get(venue.getCategory())
                                * venueCategoryConfigurationProperties.getLowerRatingBound()))
                .peek(venue -> venue.setValid(true))
                .collect(Collectors.toList());
        venues = venueTransactionalService.saveVenues(venues); // true valid flag for filtered venues
        log.info("After filtering become {} venues from categories = {}, filtering time = {} ms", venues.size(), categories, (System.currentTimeMillis() - startTime));

        double minRatingValue = venues.stream().mapToDouble(Venue::getRating).min().orElse(0.0);
        double maxRatingValue = venues.stream().mapToDouble(Venue::getRating).max().orElse(0.0);
        log.info("Begin rating normalization to [~0, ~10] interval... Min rating = {}... Max rating = {}", minRatingValue, maxRatingValue);
        venues.forEach(venue -> venue.setRating(Math.pow(venue.getRating(), 1.0 / Math.log10(maxRatingValue))));
        return venues;
    }
}
