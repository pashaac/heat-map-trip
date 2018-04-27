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
        boundingBox.setSource(source);
        boundingBox.setCategories(categoryService.join(categories));
        return venueMinerIdentifier(source).apiMine(boundingBox).orElse(Collections.emptyList());
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
        if (Objects.isNull(source)) {
            log.warn("Null source impossible! Skip it!");
            return Collections.emptyList();
        }
        if (CollectionUtils.isEmpty(categories)) {
            log.warn("Categories list to collect is empty! Skip it!");
            return Collections.emptyList();
        }
        City city = Optional.ofNullable(cityRepository.findOne(cityId))
                .orElseThrow(() -> new IllegalArgumentException("No city with id = " + cityId));
        BoundingBox boundingBox = city.getBoundingBox();
        boundingBox.setSource(source);
        boundingBox.setCategories(categoryService.join(categories));
        BoundingBox savedBoundingBox = boundingBoxService.save(boundingBox);
        log.info("Mining places for city {} by categories {} starting...", city.getCity(), categories);
        return quadTreeMine(savedBoundingBox);
    }

    public List<Venue> quadTreeMine(BoundingBox rootBoundingBox) {
        if (Objects.isNull(rootBoundingBox.getSource())) {
            log.warn("Bounding box has empty source information! Skip it!");
            return Collections.emptyList();
        }
        if (rootBoundingBox.isValid()) {
            log.warn("Bounding box has valid status flag! Skip it!");
            return Collections.emptyList();
        }

        long startTime = System.currentTimeMillis();
        VenueMiner venueMiner = venueMinerIdentifier(rootBoundingBox.getSource());
        Queue<BoundingBox> boxQueue = new ArrayDeque<>(Collections.singleton(rootBoundingBox));
        int ind = 0;
        int apiCallCounter = 0;
        List<Venue> venues = new ArrayList<>();
        while (!boxQueue.isEmpty()) {
            log.debug("Trying to get places for boundingBox #{}...", ind++);
            BoundingBox boundingBox = Optional.ofNullable(boxQueue.poll())
                    .orElseThrow(() -> new IllegalArgumentException("Null boundingBox during mining process"));
            Optional<List<Venue>> apiMinedBoundingBoxVenues = venueMiner.apiMine(boundingBox);
            ++apiCallCounter;
            if (!apiMinedBoundingBoxVenues.isPresent()) {
                log.warn("API mine call failed... Stop collection process! Scheduler will handle this area later...");
                continue;
            }
            if (venueMiner.isReachTheLimit(apiMinedBoundingBoxVenues.get())) {
                log.debug("Split bounding box due to {} API client return max amount of venues", boundingBox.getSource());
                List<BoundingBox> quarters = boundingBoxService.splitBoundingBox(boundingBox);
                boxQueue.addAll(quarters);
                continue;
            }
            // Hack to have venues strongly inside bounding box
            List<Venue> boundingBoxVenues = apiMinedBoundingBoxVenues.get().stream()
                    .filter(venue -> GeoEarthMathUtils.contains(boundingBox, venue.getLocation()))
                    .collect(Collectors.toList());
            BoundingBox savedBoundingBox = boundingBoxService.saveBoundingBoxWithVenues(boundingBox, boundingBoxVenues);
            venues.addAll(savedBoundingBox.getVenues());
            log.info("Was searched: {} {} venues", boundingBoxVenues.size(), boundingBox.getSource());
        }
        log.info("API called approximately: {} times", apiCallCounter);
        log.info("Was searched {} venues", venues.size());
        log.info("City area was scanned in {} ms to collect venues according to categories: {}", System.currentTimeMillis() - startTime, rootBoundingBox.getCategories());
        return venues;
    }

    public List<Venue> quadTreeMineIfNeeded(Long cityId, Source source, List<String> categories) {
//        TODO: Need try handle invalid boundingBoxes or not?
//        List<BoundingBox> invalidBoundingBoxes = boundingBoxService.getValidBasedBoundingBoxes(cityId, source, categories, false);
//        if (CollectionUtils.isEmpty(invalidBoundingBoxes)) {
//            log.info("No invalid bounding boxes :) continue");
//        } else {
//            log.info("Was found {} invalid bounding boxes... Let's handle its", invalidBoundingBoxes.size());
//            invalidBoundingBoxes.stream()
//                    .peek(boundingBox -> log.info("Try mine data for boundingBox with id = {} from city = {}", boundingBox.getId(), boundingBox.getCity().getCity()))
//                    .forEach(this::quadTreeMine);
//        }

        List<Venue> venues = new ArrayList<>();
        List<String> emptyCategories = new ArrayList<>();
        for (String category : categories) {
            List<BoundingBox> categoryContainsBoundingBoxes = boundingBoxService.getValidBasedBoundingBoxes(cityId, source, Collections.singletonList(category), true);
            if (CollectionUtils.isEmpty(categoryContainsBoundingBoxes)) {
                emptyCategories.add(category);
                continue;
            }
            List<Venue> categoryVenues = categoryContainsBoundingBoxes.stream()
                    .flatMap(boundingBox -> boundingBox.getVenues().stream())
                    .filter(venue -> category.equals(venue.getCategory()))
                    .collect(Collectors.toList());
            venues.addAll(categoryVenues);
        }
        List<Venue> quadTreeMinedVenues = quadTreeMine(cityId, source, emptyCategories);
        venues.addAll(quadTreeMinedVenues);
        return venues;
    }

    // TODO: need improvements in this algorithm / Use special filtering: invalidate all -> filtering -> set valid flag in db -> return
    public List<Venue> venueValidation(List<Venue> dirtyVenues) {
        long startTime = System.currentTimeMillis();
//        List<Venue> venues = dirtyVenues.stream()
//                .peek(venue -> venue.setValid(false))
//                .collect(Collectors.toList());
//        venues = venueRepository.save(venues); // invalidate all
        List<Venue> venues = dirtyVenues.stream()
                .filter(venue -> Objects.nonNull(venue.getCategory()))
                .filter(venue -> Character.isAlphabetic(venue.getTitle().charAt(0)))
                .filter(venue -> Character.isUpperCase(venue.getTitle().charAt(0)))
                .collect(Collectors.toList());

        Map<String, List<Venue>> groupedVenues = venues.stream().collect(Collectors.groupingBy(Venue::getCategory));
        Map<String, Double> averageRating = venues.stream().collect(Collectors.groupingBy(Venue::getCategory, Collectors.averagingDouble(Venue::getRating)));
        venues = groupedVenues.entrySet().stream()
                .peek(entry -> log.info("Filtering {} venues from category {}", entry.getValue().size(), entry.getKey()))
                .flatMap(entry -> entry.getValue().stream()
                        .filter(venue -> venue.getRating() > averageRating.get(venue.getCategory()) * venueCategoryConfigurationProperties.getLowerRatingBound()))
//                .peek(venue -> venue.setValid(true))
                .collect(Collectors.toList());
        venues = venueRepository.save(venues); // true valid flag for filtered venues
        log.info("After filtering become {} venues, filtering time = {} ms", venues.size(), (System.currentTimeMillis() - startTime));

        double minRatingValue = venues.stream().mapToDouble(Venue::getRating).min().orElse(0.0);
        double maxRatingValue = venues.stream().mapToDouble(Venue::getRating).max().orElse(0.0);
        log.info("Begin rating normalization to [~0, ~10] interval... Min rating = {}... Max rating = {}", minRatingValue, maxRatingValue);
        venues.forEach(venue -> venue.setRating(Math.pow(venue.getRating(), 1.0 / Math.log10(maxRatingValue))));
        return venues;
    }

    // TODO: need improve this algorithm and inject properties
    public List<Marker> calculateVenuesDistribution(List<Venue> venues) {
        long startTime = System.currentTimeMillis();
        List<Marker> markers = new ArrayList<>();
        int levelMarkersCount = 7;
        int levelMarkersAngle = 360;
        int levelAreaPart = 70;
        for (Venue venue : venues) {
            Random angleRandom = new Random();
            Random distanceRandom = new Random();
            double rating = venue.getRating();
            Marker location = venue.getLocation();
            int ratingLevel = 0;
            while (rating > 0) {
                ratingLevel++;
                int markersCount = rating < 1 ? (int) Math.round(rating * levelMarkersCount) : levelMarkersCount;
                for (int i = 0; i < markersCount; i++) {
                    int angle = angleRandom.nextInt(levelMarkersAngle);
                    int distance = distanceRandom.nextInt(ratingLevel * levelAreaPart);
                    markers.add(GeoEarthMathUtils.markerOnRadialDistance(location, angle, distance));
                }
                rating -= 1.3;
            }
        }
        log.info("Distribution process generate {} markers in {} ms based on {} venues ", markers.size(), (System.currentTimeMillis() - startTime), venues.size());
        return markers;
    }


    // TODO: Think about clustering ...
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
