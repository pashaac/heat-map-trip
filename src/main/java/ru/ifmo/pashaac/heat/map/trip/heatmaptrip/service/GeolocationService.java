package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;

import com.google.maps.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.City;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.repository.CityRepository;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils.GeoEarthMathUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    private final CityRepository cityRepository;

    @Autowired
    public GeolocationService(GoogleService googleService, CityRepository cityRepository) {
        this.googleService = googleService;
        this.cityRepository = cityRepository;
    }

    public City reverseGeolocation(Marker location) {
        log.info("Try determine city by coordinates ({}, {}) ...", location.getLatitude(), location.getLongitude());
        return cityRepository.findAll().stream()
                .filter(city -> GeoEarthMathUtils.contains(city.getBoundingBox(), location))
                .peek(city -> log.info("City ({}, {}) from database contains coordinates ({}, {})", city.getCity(), city.getCountry(), location.getLatitude(), location.getLongitude()))
                .findFirst().orElseGet(() -> reverseGeolocation(googleService.reverseGeocode(location)));
    }

    private City reverseGeolocation(GeocodingResult[] geocodingResults) {
        AddressComponent city = Arrays.stream(geocodingResults)
                .filter(geocodingResult -> Arrays.asList(geocodingResult.types).containsAll(CITY_TYPES))
                .flatMap(geocodingResult -> Arrays.stream(geocodingResult.addressComponents))
                .filter(addressComponent -> Arrays.asList(addressComponent.types).containsAll(CITY_COMPONENT_TYPES))
                .findFirst().orElseGet(() -> Arrays.stream(geocodingResults)
                        .filter(geocodingResult -> Arrays.asList(geocodingResult.types).containsAll(CITY_TYPES_RESERVE))
                        .flatMap(geocodingResult -> Arrays.stream(geocodingResult.addressComponents))
                        .filter(addressComponent -> Arrays.asList(addressComponent.types).containsAll(CITY_COMPONENT_TYPES_RESERVE))
                        .findFirst().orElseThrow(() -> new IllegalArgumentException("Could not determine city geolocation")));

        AddressComponent country = Arrays.stream(geocodingResults)
                .filter(geocodingResult -> Arrays.asList(geocodingResult.types).containsAll(COUNTRY_TYPES))
                .flatMap(geocodingResult -> Arrays.stream(geocodingResult.addressComponents))
                .filter(addressComponent -> Arrays.asList(addressComponent.types).containsAll(COUNTRY_COMPONENT_TYPES))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Could not determine country geolocation"));

        City savedCity = cityRepository.findCityByCityAndCountry(city.longName, country.longName);
        if (Objects.nonNull(savedCity)) {
            log.info("Google geolocation method determined city {}, {} in database by city.name and country.name", savedCity.getCity(), savedCity.getCountry());
            return savedCity;
        }

        Bounds box = Arrays.stream(geocodingResults)
                .filter(geocodingResult -> Arrays.asList(geocodingResult.types).containsAll(CITY_TYPES))
                .map(geocodingResult -> geocodingResult.geometry.bounds)
                .findFirst().orElseGet(() -> Arrays.stream(geocodingResults)
                        .filter(geocodingResult -> Arrays.asList(geocodingResult.types).containsAll(CITY_TYPES_RESERVE))
                        .map(geocodingResult -> geocodingResult.geometry.bounds)
                        .findFirst().orElseThrow(() -> new IllegalArgumentException("Could not determine boundingBox area")));

        BoundingBox boundingBox = new BoundingBox(new Marker(box.southwest.lat, box.southwest.lng), new Marker(box.northeast.lat, box.northeast.lng));
        savedCity = cityRepository.saveAndFlush(new City(city.longName, country.longName, boundingBox));
        log.info("Google geolocation method determined city {}, {} and saved it into database", savedCity.getCity(), savedCity.getCountry());
        return savedCity;
    }

    public Marker geolocation(String address) {
        log.info("Try determine city by address: {} ...", address);
        return GeoEarthMathUtils.center(reverseGeolocation(googleService.geocode(address)).getBoundingBox());
    }


}
