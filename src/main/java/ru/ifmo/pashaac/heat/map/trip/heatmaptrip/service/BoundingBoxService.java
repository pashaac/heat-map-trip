package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public List<BoundingBox> getValidBasedBoundingBoxes(Long cityId, Source source, List<String> categories, boolean valid) {
        Map<Long, BoundingBox> boundingBoxMap = categories.stream()
                .flatMap(category -> boundingBoxRepository.findBoundingBoxesByCity_IdAndSourceAndCategoriesContainsAndValid(cityId, source, category, valid).stream())
                .collect(Collectors.toMap(BoundingBox::getId, bbox -> bbox));
        return boundingBoxMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BoundingBox save(BoundingBox boundingBox) {
        return boundingBoxRepository.save(boundingBox);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<BoundingBox> splitBoundingBox(BoundingBox boundingBox) {
        boundingBoxRepository.delete(boundingBox);
        return boundingBoxRepository.save(GeoEarthMathUtils.getQuarters(boundingBox));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BoundingBox saveBoundingBoxWithVenues(BoundingBox boundingBox, List<Venue> venues) {
        boundingBox.setValid(true);
        boundingBox.setVenues(venues);
        return boundingBoxRepository.save(boundingBox);
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
        return boundingBoxes;

//      TODO: old version which provide bad aligning, not good accurancy. Only for demonstration
//        return IntStream.range(0, grid * grid)
//                .mapToObj(cell -> {
//                    int xWest = cell % grid;
//                    int ySouth = cell / grid;
//
//                    Marker southWest = boundingBox.getSouthWest();
//
//                    Marker southEast = GeoEarthMathUtils.getSouthEast(boundingBox);
//                    double xWestLongitude = GeoEarthMathUtils.measureOut(southWest, southEast, (double) xWest / grid).getLongitude();
//                    double xEastLongitude = GeoEarthMathUtils.measureOut(southWest, southEast, (double) (xWest + 1) / grid).getLongitude();
//
//                    Marker northWest = GeoEarthMathUtils.getNorthWest(boundingBox);
//                    double ySouthLatitude = GeoEarthMathUtils.measureOut(southWest, northWest, (double) ySouth / grid).getLatitude();
//                    double yNorthLatitude = GeoEarthMathUtils.measureOut(southWest, northWest, (double) (ySouth + 1) / grid).getLatitude();
//
//                    return new BoundingBox(new Marker(ySouthLatitude, xWestLongitude), new Marker(yNorthLatitude, xEastLongitude));
//                })
//                .collect(Collectors.toList());
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

        double step = steps/ (maxRating - minRating);
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
