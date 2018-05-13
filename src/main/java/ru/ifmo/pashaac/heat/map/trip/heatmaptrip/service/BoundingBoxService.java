package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.ClusterableBoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.repository.BoundingBoxRepository;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.repository.CityRepository;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils.GeoEarthMathUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
@Service
public class BoundingBoxService {

    private final BoundingBoxRepository boundingBoxRepository;
    private final CityRepository cityRepository;

    @Autowired
    public BoundingBoxService(BoundingBoxRepository boundingBoxRepository, CityRepository cityRepository) {
        this.boundingBoxRepository = boundingBoxRepository;
        this.cityRepository = cityRepository;
    }

    public BoundingBox getBoundingBox(Long boundingBoxId) {
        return boundingBoxRepository.findOne(boundingBoxId);
    }

    public List<BoundingBox> getTotalInvalidBoundingBoxes() {
        return boundingBoxRepository.findBoundingBoxesByValidIsFalse();
    }

    public List<BoundingBox> getBoundingBoxes(Long cityId, Source source, List<String> categories, boolean valid) {
        return categories.stream()
                .flatMap(category -> boundingBoxRepository.findBoundingBoxesByCity_IdAndSourceAndCategoryAndValid(cityId, source, category, valid).stream())
                .collect(Collectors.toList());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BoundingBox save(BoundingBox boundingBox) {
        return boundingBoxRepository.save(boundingBox);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<BoundingBox> save(List<BoundingBox> boundingBoxes) {
        return boundingBoxRepository.save(boundingBoxes);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void remove(BoundingBox boundingBox) {
        boundingBoxRepository.delete(boundingBox);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<BoundingBox> splitBoundingBox(BoundingBox boundingBox) {
        boundingBoxRepository.delete(boundingBox);
        return boundingBoxRepository.save(GeoEarthMathUtils.getQuarters(boundingBox));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<BoundingBox> replaceBoundingBox(BoundingBox boundingBox, List<BoundingBox> children) {
        boundingBoxRepository.delete(boundingBox);
        return boundingBoxRepository.save(children);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BoundingBox saveBoundingBoxWithVenues(BoundingBox boundingBox, List<Venue> venues) {
        boundingBox.setValid(true);
        boundingBox.setVenues(venues);
        return boundingBoxRepository.save(boundingBox);
    }

    public List<BoundingBox> stupidIncrementGridBoundingBox(Long cityId, int grid) {
        BoundingBox boundingBox = cityRepository.findOne(cityId).getBoundingBox();
        Marker sw = boundingBox.getSouthWest();
        Marker se = GeoEarthMathUtils.getSouthEast(boundingBox);
        Marker nw = GeoEarthMathUtils.getNorthWest(boundingBox);

        double sweDistance = GeoEarthMathUtils.distance(sw, se);
        double wsnDistance = GeoEarthMathUtils.distance(sw, nw);
        double sweStep = sweDistance / grid;
        double wsnStep = wsnDistance / grid;

        List<BoundingBox> boundingBoxes = new ArrayList<>();
        Marker x = sw, xNext = sw;
        for (int i = 0; i < grid; i++) {
            x = xNext;
            xNext = GeoEarthMathUtils.markerOnRadialDistance(x, 90, sweStep);
            Marker y = x, yNext = x;
            for (int j = 0; j < grid; j++) {
                y = yNext;
                yNext = GeoEarthMathUtils.markerOnRadialDistance(y, 0, wsnStep);
                boundingBoxes.add(new BoundingBox(new Marker(y.getLatitude(), x.getLongitude()),
                        new Marker(yNext.getLatitude(), xNext.getLongitude())));
            }
        }
        return boundingBoxes;
    }

    public List<BoundingBox> stupidStreamGridBoundingBox(Long cityId, int grid) {
        BoundingBox boundingBox = cityRepository.findOne(cityId).getBoundingBox();
        return IntStream.range(0, grid * grid)
                .mapToObj(cell -> {
                    int x = cell % grid;
                    int y = cell / grid;
                    Marker sw = boundingBox.getSouthWest();
                    Marker se = GeoEarthMathUtils.getSouthEast(boundingBox);
                    double xWestLongitude = GeoEarthMathUtils.measureOut(sw, se, (double) x / grid).getLongitude();
                    double xEastLongitude = GeoEarthMathUtils.measureOut(sw, se, (double) (x + 1) / grid).getLongitude();
                    Marker northWest = GeoEarthMathUtils.getNorthWest(boundingBox);
                    double ySouthLatitude = GeoEarthMathUtils.measureOut(sw, northWest, (double) y / grid).getLatitude();
                    double yNorthLatitude = GeoEarthMathUtils.measureOut(sw, northWest, (double) (y + 1) / grid).getLatitude();
                    return new BoundingBox(new Marker(ySouthLatitude, xWestLongitude), new Marker(yNorthLatitude, xEastLongitude));
                })
                .collect(Collectors.toList());
    }


    public List<BoundingBox> gridBoundingBox(Long cityId, int grid) {
        BoundingBox boundingBox = cityRepository.findOne(cityId).getBoundingBox();
        List<BoundingBox> boundingBoxes = new ArrayList<>();
        for (int xWest = 0; xWest < grid; xWest++) {
            Marker xSouthWest = boundingBox.getSouthWest();
            Marker boundingBoxNorthEast = boundingBox.getNorthEast();
            for (int i = 0; i < xWest; i++) {
                Marker southEast = GeoEarthMathUtils.getSouthEast(new BoundingBox(xSouthWest, boundingBoxNorthEast));
                xSouthWest = GeoEarthMathUtils.measureOut(xSouthWest, southEast, 1.0 / (grid - i));
            }
            for (int ySouth = 0; ySouth < grid; ySouth++) {
                Marker southWest = new Marker(xSouthWest.getLatitude(), xSouthWest.getLongitude());
                for (int i = 0; i < ySouth; i++) {
                    Marker bboxNorthWest = GeoEarthMathUtils.getNorthWest(new BoundingBox(southWest, boundingBoxNorthEast));
                    southWest = GeoEarthMathUtils.measureOut(southWest, bboxNorthWest, 1.0 / (grid - i));
                }
                Marker southEast = GeoEarthMathUtils.getSouthEast(new BoundingBox(southWest, boundingBoxNorthEast));
                southEast = GeoEarthMathUtils.measureOut(southWest, southEast, 1.0 / (grid - xWest));
                Marker northEast = GeoEarthMathUtils.getNorthWest(new BoundingBox(southEast, boundingBoxNorthEast)); // yes, northEast
                northEast = GeoEarthMathUtils.measureOut(southEast, northEast, 1.0 / (grid - ySouth));

                boundingBoxes.add(new BoundingBox(southWest, northEast));
            }
        }
        log.info("During GRID process was generated {} bounding boxes", boundingBoxes.size());
        return boundingBoxes;
    }

    public double calculateAveragePleasure(List<Venue> venues) {
        double totalRating = venues.stream().mapToDouble(Venue::getRating).sum();
        double totalDistance = 0;
        for (int i = 0; i < venues.size(); i++) {
            for (int j = i + 1; j < venues.size(); j++) {
                totalDistance += GeoEarthMathUtils.distance(venues.get(i).getLocation(), venues.get(j).getLocation());
            }
        }
        log.info("Total rating: {}", totalRating);
        log.info("Total distance: {}", totalDistance);
        double averageDistance = totalDistance / (venues.size() * (venues.size() - 1) / 2);
        double averageRating = totalRating / venues.size();
        log.info("Average distance: {}", averageDistance);
        log.info("Average rating : {}", averageRating);
        log.info("Pleasure (in meters): {}", averageRating / averageDistance);
        return averageRating / averageDistance;
    }

    public List<ClusterableBoundingBox> smileClustering(List<Venue> venues, List<BoundingBox> boundingBoxes, double pleasure) {
        List<ClusterableBoundingBox> clusterableBoundingBoxes = new ArrayList<>();
        for (int i = 0; i < boundingBoxes.size(); i++) {
            double rating = 0;
            double count = 0;
            int outerRadius = GeoEarthMathUtils.outerRadius(boundingBoxes.get(i));
            int maxDistance = 3 * outerRadius;
            for (Venue venue : venues) {
                if (GeoEarthMathUtils.contains(boundingBoxes.get(i), venue.getLocation())) {
                    rating += venue.getRating();
                    count += 1;
                    continue;
                }
                double distance = GeoEarthMathUtils.distance(GeoEarthMathUtils.center(boundingBoxes.get(i)), venue.getLocation());
                if (distance < maxDistance) {
                    rating += venue.getRating() * (maxDistance - distance) / maxDistance;
                    count += (maxDistance - distance) / maxDistance;
                }
//              Possible find another formulas if scan git history of this file
            }
            clusterableBoundingBoxes.add(new ClusterableBoundingBox(i, count < 0.001 ? 0 : rating / Math.sqrt(count), null));
        }

        KMeansPlusPlusClusterer<ClusterableBoundingBox> clusterer = new KMeansPlusPlusClusterer<>(5, 10_000, new ClusterableBoundingBox(), new JDKRandomGenerator(), KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_POINTS_NUMBER);
        List<CentroidCluster<ClusterableBoundingBox>> clusters = clusterer.cluster(clusterableBoundingBoxes);
        log.info("Clusters count: {}", clusters.size());
        List<String> colors = Stream.of(Color.gray, new Color(173, 255, 47), Color.yellow, new Color(255, 140, 0), Color.red)
                .map(color -> String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()))
                .collect(Collectors.toList());

        List<Double> sortedCenters = clusters.stream()
                .mapToDouble(cluster -> cluster.getCenter().getPoint()[0])
                .sorted()
                .boxed()
                .collect(Collectors.toList());

        clusterableBoundingBoxes.clear();
        for (int i = 0; i < sortedCenters.size(); i++) {
            String color = colors.get(i);
            if (String.format("#%02x%02x%02x", Color.gray.getRed(), Color.gray.getGreen(), Color.gray.getBlue()).equals(color)) {
                continue;
            }
            Double center = sortedCenters.get(i);
            for (CentroidCluster<ClusterableBoundingBox> cluster : clusters) {
                if (Math.abs(cluster.getCenter().getPoint()[0] - center) < 0.001) {
                    cluster.getPoints().forEach(point -> point.setColor(color));
                    clusterableBoundingBoxes.addAll(cluster.getPoints());
                }
            }
        }
        return clusterableBoundingBoxes;
    }

    public List<String> gridHeatMap(List<Venue> venues, List<BoundingBox> boundingBoxes, int grid) {
        double[] boundingBoxRatings = new double[boundingBoxes.size()];
        for (Venue venue : venues) {
            int vX = 0;
            int vY = 0;
            for (int i = 0; i < boundingBoxes.size(); i++) {
                if (GeoEarthMathUtils.contains(boundingBoxes.get(i), venue.getLocation())) {
                    vX = i % grid;
                    vY = i / grid;
                    break;
                }
            }
            for (int i = 0; i < boundingBoxes.size(); i++) {
                int x = i % grid;
                int y = i / grid;
                if (x == vX && y == vY) {
                    boundingBoxRatings[i] += venue.getRating();
                    continue;
                }
//                double distance = Math.sqrt(Math.sqrt(Math.pow(x - vX, 2) + Math.pow(y - vY, 2)));
//                double v = venue.getRating() / distance;
//                if (v > 1) {
//                    boundingBoxRatings[i] += v;
//                    count[i]++;
//                }
            }
        }
        double minRatingValue = venues.stream().mapToDouble(Venue::getRating).min().orElse(0.0);
        double maxRatingValue = venues.stream().mapToDouble(Venue::getRating).max().orElse(0.0);
        log.info("Begin rating normalization for bounding boxes to [~0, ~10] interval... Min rating = {}... Max rating = {}", minRatingValue, maxRatingValue);
        for (int i = 0; i < boundingBoxRatings.length; i++) {
            boundingBoxRatings[i] = Math.pow(boundingBoxRatings[i], 1.0 / Math.log10(maxRatingValue));
        }

        Color green = Color.yellow;
        Color red = Color.red;
        int steps = 10;
        List<String> colors = IntStream.rangeClosed(0, steps)
                .mapToObj(i -> {
                    float ratio = (float) i / (float) steps;
                    int redInt = (int) (red.getRed() * ratio + green.getRed() * (1 - ratio));
                    int greenInt = (int) (red.getGreen() * ratio + green.getGreen() * (1 - ratio));
                    int blueInt = (int) (red.getBlue() * ratio + green.getBlue() * (1 - ratio));
                    return String.format("#%02x%02x%02x", redInt, greenInt, blueInt);
                })
                .collect(Collectors.toList());
        double maxRating = Arrays.stream(boundingBoxRatings).max().orElse(0.0);
        double minRating = Arrays.stream(boundingBoxRatings).min().orElse(0.0);

        double step = steps / (maxRating - minRating);
        return Arrays.stream(boundingBoxRatings)
                .mapToObj(rating -> {
                    int index = (int) Math.round(step * (rating - minRating));
                    if (index < 2) {
                        return null;
                    }
                    return colors.get(index);
                })
                .collect(Collectors.toList());
    }

}
