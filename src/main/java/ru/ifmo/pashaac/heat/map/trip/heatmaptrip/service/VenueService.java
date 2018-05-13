package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.configuration.properties.VenueCategoryConfigurationProperties;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.ColorMarker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.City;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.repository.CityRepository;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.repository.VenueRepository;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils.GeoEarthMathUtils;
import smile.clustering.DBScan;
import smile.math.distance.Distance;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final VenueRepository venueRepository;

    @Autowired
    public VenueService(GoogleService googleService, FoursquareService foursquareService, CategoryService categoryService, CityRepository cityRepository, BoundingBoxService boundingBoxService, VenueCategoryConfigurationProperties venueCategoryConfigurationProperties, VenueRepository venueRepository) {
        this.googleService = googleService;
        this.foursquareService = foursquareService;
        this.categoryService = categoryService;
        this.cityRepository = cityRepository;
        this.boundingBoxService = boundingBoxService;
        this.venueCategoryConfigurationProperties = venueCategoryConfigurationProperties;
        this.venueRepository = venueRepository;
    }

    public List<Venue> getVenues(List<Long> venueIds) {
        return venueRepository.findAll(venueIds);
    }

    public List<Venue> apiMine(BoundingBox boundingBox, Source source, List<String> categories) {
        List<BoundingBox> boundingBoxes = categoryService.map(categories).stream()
                .flatMap(category -> categoryService.getCategoryTypes(category, source).stream()
                        .map(type -> new BoundingBox(boundingBox, source, category, type)))
                .collect(Collectors.toList());
        log.info("API mine for {} source will be cost {} rest calls", source, boundingBoxes.size());
        return boundingBoxes.stream()
                .flatMap(bbox -> venueMinerIdentifier(source).apiMine(bbox).orElse(Collections.emptyList()).stream())
                .collect(Collectors.toList());
    }

    private VenueMiner venueMinerIdentifier(Source source) {
        switch (source) {
            case FOURSQUARE:
                return foursquareService;
            case GOOGLE:
                return googleService;
            default:
                throw new IllegalArgumentException("Incorrect data source type: " + source);
        }
    }

    private List<Venue> quadTreeMine(Long cityId, Source source, List<String> categories) {
        if (Objects.isNull(source) || CollectionUtils.isEmpty(categories)) {
            log.warn("QuadTreeMine impossible for null source or empty list categories, skip it!");
            return Collections.emptyList();
        }
        City city = Optional.ofNullable(cityRepository.findOne(cityId))
                .orElseThrow(() -> new IllegalArgumentException("No city with id = " + cityId));
        List<BoundingBox> boundingBoxes = categoryService.map(categories).stream()
                .flatMap(category -> categoryService.getCategoryTypes(category, source).stream()
                        .map(type -> new BoundingBox(city, source, category, type)))
                .collect(Collectors.toList());
        log.info("Mining places for city {} by source {} and categories {} starting...", city.getCity(), source, categories);
        return quadTreeMine(boundingBoxService.save(boundingBoxes));
    }

    public List<Venue> quadTreeMine(List<BoundingBox> boundingBoxes) {
        long startTime = System.currentTimeMillis();
        int ind = 0;
        int apiCallCounter = 0;
        List<Venue> venues = new ArrayList<>();
        Queue<BoundingBox> boxQueue = new ArrayDeque<>(boundingBoxes);
        while (!boxQueue.isEmpty()) {
            BoundingBox boundingBox = boxQueue.poll();
            if (boundingBox == null) {
                log.warn("Impossible case, but queue already empty...");
                break;
            }
            ind++;
            log.debug("Trying mine places for boundingBox #{} in city {} by source {} and category {} and type {}  ...", ind,
                    boundingBox.getCity().getCity(), boundingBox.getSource(), boundingBox.getCategory(), boundingBox.getType());
            if (Objects.isNull(boundingBox.getSource()) || Objects.isNull(boundingBox.getCategory()) || Objects.isNull(boundingBox.getType()) || boundingBox.isValid()) {
                log.warn("Bounding box has empty source or category or type or valid status, skip it and remove!");
                boundingBoxService.remove(boundingBox);
                continue;
            }
            VenueMiner venueMiner = venueMinerIdentifier(boundingBox.getSource());
            Optional<List<Venue>> apiMinedBoundingBoxVenues = venueMiner.apiMine(boundingBox);
            ++apiCallCounter;
            if (!apiMinedBoundingBoxVenues.isPresent()) {
                log.warn("API mine call failed... Collection for this bounding box is stopped! Scheduler will handle this area later...");
                continue;
            }
            if (venueMiner.isReachTheLimit(apiMinedBoundingBoxVenues.get())) {
                log.debug("Split bounding box #{} on quarters due to max amount of venues from API client", ind);
                List<BoundingBox> quarters = boundingBoxService.splitBoundingBox(boundingBox);
                boxQueue.addAll(quarters);
                continue;
            }
            List<Venue> boundingBoxVenues = apiMinedBoundingBoxVenues.get().stream()
                    .filter(venue -> GeoEarthMathUtils.contains(boundingBox, venue.getLocation()))
                    .filter(venue -> venue.getRating() > 0)
                    .collect(Collectors.toList());
            BoundingBox savedBoundingBox = boundingBoxService.saveBoundingBoxWithVenues(boundingBox, boundingBoxVenues);
            venues.addAll(savedBoundingBox.getVenues());
            log.info("Was searched: {} {} venues with positive rating", boundingBoxVenues.size(), boundingBox.getSource());
        }
        log.info("API called approximately: {} times", apiCallCounter);
        log.info("Was searched {} venues with positive rating", venues.size());
        String categories = boundingBoxes.stream()
                .map(BoundingBox::getCategory)
                .distinct()
                .collect(Collectors.joining(",", "[", "]"));
        log.info("City area was scanned in {} ms to collect venues according to categories: {}", System.currentTimeMillis() - startTime, categories);
        return venues;
    }

    public List<Venue> quadTreeMineIfNeeded(Long cityId, Source source, List<String> categories) {
//        TODO: Open questions about necessity handle invalid boundingBoxes in this function?
//        List<BoundingBox> invalidBoundingBoxes = boundingBoxService.getBoundingBoxes(cityId, source, categories, false);
//        if (CollectionUtils.isEmpty(invalidBoundingBoxes)) {
//            log.info("No invalid bounding boxes :) continue");
//        } else {
//            log.info("Was found {} invalid bounding boxes... Let's handle its", invalidBoundingBoxes.size());
//            invalidBoundingBoxes.stream()
//                    .peek(boundingBox -> log.info("Try mine data for boundingBox with id = {} from city = {}", boundingBox.getId(), boundingBox.getCity().getCity()))
//                    .forEach(this::quadTreeMine);
//        }
        List<Venue> venues = new ArrayList<>();
        List<String> categoriesToMine = new ArrayList<>();
        for (String category : categories) {
            List<BoundingBox> boundingBoxes = boundingBoxService.getBoundingBoxes(cityId, source, Collections.singletonList(category), true);
            if (CollectionUtils.isEmpty(boundingBoxes)) {
                categoriesToMine.add(category);
                continue;
            }
            List<Venue> categoryVenues = boundingBoxes.stream()
                    .flatMap(boundingBox -> boundingBox.getVenues().stream())
                    .collect(Collectors.toList());
            venues.addAll(categoryVenues);
        }
        List<Venue> quadTreeMinedVenues = quadTreeMine(cityId, source, categoriesToMine);
        venues.addAll(quadTreeMinedVenues);
        return venues;
    }

    // TODO: Think about idea to use special filtering: invalidate all -> filtering -> set valid flag in db -> return
    public List<Venue> venueValidation(List<Venue> dirtyVenues) {
        long startTime = System.currentTimeMillis();
        List<Venue> venues = dirtyVenues.stream()
                .filter(Venue::isValid)
                .filter(venue -> Character.isAlphabetic(venue.getTitle().charAt(0)))
                .filter(venue -> Character.isUpperCase(venue.getTitle().charAt(0)))
                .collect(Collectors.toList());

        Map<String, List<Venue>> groupedVenues = venues.stream().collect(Collectors.groupingBy(venue -> venue.getBoundingBox().getCategory()));
        Map<String, Double> averageRating = venues.stream().collect(Collectors.groupingBy(venue -> venue.getBoundingBox().getCategory(), Collectors.averagingDouble(Venue::getRating)));
        venues = groupedVenues.entrySet().stream()
                .peek(entry -> log.info("Filtering {} venues from category {}", entry.getValue().size(), entry.getKey()))
                .flatMap(entry -> entry.getValue().stream()
                        .filter(venue -> venue.getRating() > averageRating.get(venue.getBoundingBox().getCategory()) * venueCategoryConfigurationProperties.getLowerRatingBound()))
                .collect(Collectors.toList());
        log.info("After filtering become {} venues, filtering time = {} ms", venues.size(), (System.currentTimeMillis() - startTime));

        double minRatingValue = venues.stream().mapToDouble(Venue::getRating).min().orElse(0.0);
        double maxRatingValue = venues.stream().mapToDouble(Venue::getRating).max().orElse(0.0);
        log.info("Begin rating normalization to [~0, ~10] interval... Min rating = {}... Max rating = {}", minRatingValue, maxRatingValue);
        venues.forEach(venue -> venue.setRating(Math.pow(venue.getRating(), 1.0 / Math.log10(maxRatingValue))));
        return venues;
    }

    // TODO: Think about some algorithm improvements and inject properties
    public List<Marker> calculateVenuesDistribution(List<Venue> venues) {
        long startTime = System.currentTimeMillis();
        Random angleRandom = new Random();
        Random distanceRandom = new Random();
        List<Marker> markers = new ArrayList<>();
        for (Venue venue : venues) {
            Marker location = venue.getLocation();
            for (int i = 1; i <= Math.round(venue.getRating()); i++) {
                for (int j = 0; j < venueCategoryConfigurationProperties.getDistributionCount() + i; j++) {
                    int distance = distanceRandom.nextInt(i * venueCategoryConfigurationProperties.getDistributionArea());
                    int angle = angleRandom.nextInt(360);
                    markers.add(GeoEarthMathUtils.markerOnRadialDistance(location, angle, distance));
                }
            }
        }
        log.info("Distribution process generate {} markers in {} ms based on {} venues", markers.size(), (System.currentTimeMillis() - startTime), venues.size());
        log.info("Let's remove circles without intersections...");
        startTime = System.currentTimeMillis();
        List<Marker> intersectionMarkers = new ArrayList<>();
        for (int i = 0; i < markers.size(); i++) {
            int intersectionCount = 0;
            boolean intersectionFlag = false;
            for (int j = i + 1; j < markers.size(); j++) {
                if (GeoEarthMathUtils.distance(markers.get(i), markers.get(j)) < venueCategoryConfigurationProperties.getDistributionIntersectionDistance()) {
                    if (++intersectionCount == venueCategoryConfigurationProperties.getDistributionCount()) {
                        intersectionFlag = true;
                        break;
                    }
                }
            }
            if (intersectionFlag) {
                intersectionMarkers.add(markers.get(i));
            }
        }
        log.info("Distribution process after intersection filter provide {} markers in {} ms based on {} markers", intersectionMarkers.size(), (System.currentTimeMillis() - startTime), markers.size());
        return intersectionMarkers;
    }


    // Clustering with apache math library
    public List<ColorMarker> apacheMathClustering(List<Marker> markers) {
        DBSCANClusterer<ColorMarker> clusterer = new DBSCANClusterer<>(200, 40, new ColorMarker());
        List<Cluster<ColorMarker>> clusters = clusterer.cluster(markers.stream().map(ColorMarker::new).collect(Collectors.toList()));
        List<String> colors = Stream.of(Color.red, Color.gray, Color.blue, Color.black, Color.cyan, Color.green, Color.magenta, Color.orange, Color.yellow)
                .map(color -> String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()))
                .collect(Collectors.toList());
        Random random = new Random();
        ArrayList<ColorMarker> points = new ArrayList<>();
        for (Cluster<ColorMarker> cluster : clusters) {
            int index = random.nextInt(colors.size());
            log.info("Cluster: points = {}, color = {}", cluster.getPoints().size(), colors.get(index));
            cluster.getPoints().forEach(point -> point.setColor(colors.get(index)));
            points.addAll(cluster.getPoints());
        }
        log.info("Clustering finished!");
        return points;
    }

    // CLustering with smile clustering library
    public List<ColorMarker> smileClustering(List<Marker> markers) {
        ColorMarker[] array = markers.stream().map(ColorMarker::new).toArray(ColorMarker[]::new);
        Arrays.stream(array).limit(30).map(marker -> marker.getLatitude() + " " + marker.getLongitude()).forEach(System.out::println);
        long start = System.currentTimeMillis();
        DBScan<ColorMarker> colorMarkerDBScan = new DBScan<>(array, (Distance<ColorMarker>) (x, y)
                -> GeoEarthMathUtils.distance(new Marker(x.getLatitude(), x.getLongitude()), new Marker(y.getLatitude(), y.getLongitude())), 25, 500);
        log.info("Clustering take {} ms and calculated {} clusters", System.currentTimeMillis() - start, colorMarkerDBScan.getNumClusters());

        List<String> colors = Stream.of(Color.red, Color.gray, Color.blue, Color.black, Color.cyan, Color.green, Color.magenta, Color.orange, Color.yellow)
                .map(color -> String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()))
                .collect(Collectors.toList());

        Random random = new Random();
        for (int cluster = 0; cluster < colorMarkerDBScan.getNumClusters(); cluster++) {
            log.info("Cluster #{} contains {} places", cluster, colorMarkerDBScan.getClusterSize()[cluster]);
            String color = colors.get(random.nextInt(colors.size()));
            int[] clusterLabel = colorMarkerDBScan.getClusterLabel();
            for (int i = 0; i < clusterLabel.length; i++) {
                if (clusterLabel[i] == cluster) {
                    array[i].setColor(color);
                }
            }
        }
        return Arrays.stream(array).filter(marker -> Objects.nonNull(marker.getColor())).collect(Collectors.toList());
    }

}
