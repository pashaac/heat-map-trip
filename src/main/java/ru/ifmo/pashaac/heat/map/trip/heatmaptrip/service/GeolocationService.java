package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;

import com.google.maps.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.City;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils.GeoEarthMathUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Pavel Asadchiy
 * on 23:05 29.03.18.
 */
@Slf4j
@Service
public class GeolocationService {

    private static final List<AddressComponentType> CITY_COMPONENT_TYPES = Arrays.asList(AddressComponentType.LOCALITY, AddressComponentType.POLITICAL);
    private static final List<AddressType> CITY_TYPES = Arrays.asList(AddressType.LOCALITY, AddressType.POLITICAL);

    private static final List<AddressComponentType> CITY_COMPONENT_TYPES_RESERVE = Arrays.asList(AddressComponentType.ADMINISTRATIVE_AREA_LEVEL_2, AddressComponentType.POLITICAL);
    private static final List<AddressType> CITY_TYPES_RESERVE = Arrays.asList(AddressType.ADMINISTRATIVE_AREA_LEVEL_2, AddressType.POLITICAL);


    private static final List<AddressComponentType> COUNTRY_COMPONENT_TYPES = Arrays.asList(AddressComponentType.COUNTRY, AddressComponentType.POLITICAL);
    private static final List<AddressType> COUNTRY_TYPES = Arrays.asList(AddressType.COUNTRY, AddressType.POLITICAL);

    private final GoogleService googleService;

    @Autowired
    public GeolocationService(GoogleService googleService) {
        this.googleService = googleService;
    }

    public City reverseGeolocation(Marker location) {
        log.info("Try determine city by coordinates ({}, {}) ...", location.getLatitude(), location.getLongitude());
        return reverseGeolocation(googleService.reverseGeocode(location));
    }

    private City reverseGeolocation(GeocodingResult[] geocodingResults) {
        AddressComponent cityComponent = Arrays.stream(geocodingResults)
                .filter(geocodingResult -> Arrays.asList(geocodingResult.types).containsAll(CITY_TYPES))
                .flatMap(geocodingResult -> Arrays.stream(geocodingResult.addressComponents))
                .filter(addressComponent -> Arrays.asList(addressComponent.types).containsAll(CITY_COMPONENT_TYPES))
                .findFirst().orElseGet(() -> Arrays.stream(geocodingResults)
                        .filter(geocodingResult -> Arrays.asList(geocodingResult.types).containsAll(CITY_TYPES_RESERVE))
                        .flatMap(geocodingResult -> Arrays.stream(geocodingResult.addressComponents))
                        .filter(addressComponent -> Arrays.asList(addressComponent.types).containsAll(CITY_COMPONENT_TYPES_RESERVE))
                        .findFirst().orElseThrow(() -> new IllegalArgumentException("Can't determine city geolocation by coordinates")));

        Bounds box = Arrays.stream(geocodingResults)
                .filter(geocodingResult -> Arrays.asList(geocodingResult.types).containsAll(CITY_TYPES))
                .map(geocodingResult -> geocodingResult.geometry.bounds)
                .findFirst().orElseGet(() -> Arrays.stream(geocodingResults)
                        .filter(geocodingResult -> Arrays.asList(geocodingResult.types).containsAll(CITY_TYPES_RESERVE))
                        .map(geocodingResult -> geocodingResult.geometry.bounds)
                        .findFirst().orElseThrow(() -> new IllegalArgumentException("Can't determine city boundingbox by coordinates")));

        AddressComponent countryComponent = Arrays.stream(geocodingResults)
                .filter(geocodingResult -> Arrays.asList(geocodingResult.types).containsAll(COUNTRY_TYPES))
                .flatMap(geocodingResult -> Arrays.stream(geocodingResult.addressComponents))
                .filter(addressComponent -> Arrays.asList(addressComponent.types).containsAll(COUNTRY_COMPONENT_TYPES))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Can't determine country geolocation by coordinates"));


        BoundingBox boundingBox = new BoundingBox(new Marker(box.southwest.lat, box.southwest.lng), new Marker(box.northeast.lat, box.northeast.lng));
        City city = new City(cityComponent.longName, countryComponent.longName, boundingBox);
        log.info("Google geolocation method determined city {}, {} and saved it into database", city.getCity(), city.getCountry());
        return city;
    }

    public Marker geolocation(String address) {
        log.info("Try determine city by address {} ...", address);
        return GeoEarthMathUtils.center(reverseGeolocation(googleService.geocode(address)).getBoundingBox());
    }

    public List<BoundingBox> grid(BoundingBox boundingBox, int grid) {
        return IntStream.range(0, grid * grid)
                .mapToObj(i -> gridBoundingBox(boundingBox, i, grid))
                .collect(Collectors.toList());
    }

    private BoundingBox gridBoundingBox(BoundingBox boundingBox, int ind, int grid) {
        int xWest = ind % grid;
        int ySouth = ind / grid;

        int xEast = xWest + 1;
        int yNorth = ySouth + 1;

        Marker southWest = boundingBox.getSouthWest();

        Marker southEast = GeoEarthMathUtils.getSouthEast(boundingBox);
        double xWestLongitude = GeoEarthMathUtils.measureOut(southWest, southEast, (double) xWest / grid).getLongitude();
        double xEastLongitude = GeoEarthMathUtils.measureOut(southWest, southEast, (double) xEast / grid).getLongitude();

        Marker northWest = GeoEarthMathUtils.getNorthWest(boundingBox);
        double ySouthLatitude = GeoEarthMathUtils.measureOut(southWest, northWest, (double) ySouth / grid).getLatitude();
        double yNorthLatitude = GeoEarthMathUtils.measureOut(southWest, northWest, (double) yNorth / grid).getLatitude();

        return new BoundingBox(new Marker(ySouthLatitude, xWestLongitude), new Marker(yNorthLatitude, xEastLongitude));
    }
}
