package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.City;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.repository.BoundingBoxRepository;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.repository.CityRepository;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils.GeoEarthMathUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoundingBoxService {

    private final CityRepository cityRepository;
    private final BoundingBoxRepository boundingBoxRepository;

    @Autowired
    public BoundingBoxService(CityRepository cityRepository, BoundingBoxRepository boundingBoxRepository) {
        this.cityRepository = cityRepository;
        this.boundingBoxRepository = boundingBoxRepository;
    }

    public List<BoundingBox> getFailBoundingBoxes(Long cityId) {
        City city = cityRepository.findOne(cityId);
        return city.getBoundingBoxes().stream()
                .filter(boundingBox -> !boundingBox.isCollect())
                .collect(Collectors.toList());
    }

    public List<BoundingBox> getFailBoundingBoxes() {
        return boundingBoxRepository.findBoundingBoxesByCollectIsFalse();
    }

    public List<BoundingBox> getSuccessBoundingBoxes(Long cityId) {
        City city = cityRepository.findOne(cityId);
        return city.getBoundingBoxes().stream()
                .filter(BoundingBox::isCollect)
                .collect(Collectors.toList());
    }

    public List<BoundingBox> gridBoundingBox(BoundingBox boundingBox, int grid) {
        List<BoundingBox> boundingBoxes = new ArrayList<>();
        for (int xWest = 0; xWest < grid; xWest++) {
            Marker xSouthWest = boundingBox.getSouthWest();
            Marker boundingBoxNorthEast = boundingBox.getNorthEast();
            for (int i = 0; i < xWest; i++) {
                Marker southEast = GeoEarthMathUtils.getSouthEast(new BoundingBox(xSouthWest, boundingBoxNorthEast));
                xSouthWest = GeoEarthMathUtils.measureOut(xSouthWest, southEast,  1.0 / (grid - i));
            }
            for (int ySouth = 0; ySouth < grid; ySouth++) {
                Marker southWest = new Marker(xSouthWest.getLatitude(), xSouthWest.getLongitude());
                for (int i = 0; i < ySouth; i++) {
                    Marker bboxNorthWest = GeoEarthMathUtils.getNorthWest(new BoundingBox(southWest, boundingBoxNorthEast));
                    southWest = GeoEarthMathUtils.measureOut(southWest, bboxNorthWest,  1.0 / (grid - i));
                }

                Marker southEast = GeoEarthMathUtils.getSouthEast(new BoundingBox(southWest, boundingBoxNorthEast));
                southEast = GeoEarthMathUtils.measureOut(southWest, southEast,  1.0 / (grid - xWest));

                Marker northEast = GeoEarthMathUtils.getNorthWest(new BoundingBox(southEast, boundingBoxNorthEast)); // yes, northEast
                northEast = GeoEarthMathUtils.measureOut(southEast, northEast, 1.0 / (grid - ySouth));

                boundingBoxes.add(new BoundingBox(southWest, northEast));
            }
        }
        return boundingBoxes;

//      Old version which provide bad aligning, not good accurancy
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
}
