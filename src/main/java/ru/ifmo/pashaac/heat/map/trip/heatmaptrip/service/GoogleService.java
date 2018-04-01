package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;

import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.client.GoogleClient;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.configuration.properties.GoogleConfigurationProperties;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Category;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils.GeoEarthMathUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Pavel Asadchiy
 * on 14:09 24.03.18.
 */
@Slf4j
@Service
public class GoogleService extends AbstractVenueMiner {

    private static final String CATEGORIES_SEPARATOR = "|";

    private final GoogleClient googleClient;
    private final GoogleConfigurationProperties googleConfigurationProperties;

    @Autowired
    public GoogleService(GoogleClient googleClient, GoogleConfigurationProperties googleConfigurationProperties) {
        this.googleClient = googleClient;
        this.googleConfigurationProperties = googleConfigurationProperties;
    }

    @Override
    public List<Venue> apiCall(Marker center, int radius, List<Category> categories) {
        try {
            String googleApiCategories = googleApiUnderstandableCategories(categories);
            List<Venue> venues = googleClient.apiCall(center, radius, googleApiCategories);
            log.info("Google API call return {} venues according to categories: {}", venues.size(), googleApiCategories);
            return venues;
        } catch (InterruptedException | ApiException | IOException e) {
            log.error("Google API service temporary unavailable or reject call, message {}", e.getMessage());
            throw new RuntimeException("Google API service temporary unavailable");
        }
    }

    private String googleApiUnderstandableCategories(List<Category> categories) {
        return categories.stream()
                .map(Category::getGoogleKey)
                .collect(Collectors.joining(CATEGORIES_SEPARATOR));
    }

    public GeocodingResult[] reverseGeocode(Marker location) {
        try {
            return googleClient.reverseGeocode(new LatLng(location.getLatitude(), location.getLongitude()));
        } catch (ApiException | InterruptedException | IOException e) {
            throw new RuntimeException(String.format("Error was happened due to geocoding by coordinates (%s, %s)", location.getLongitude(), location.getLongitude()), e);
        }
    }

    public GeocodingResult[] geocode(String address) {
        try {
            return googleClient.geocode(address);
        } catch (ApiException | InterruptedException | IOException e) {
            throw new RuntimeException(String.format("Error was happened due to geocoding by address: %s", address), e);
        }
    }

    private List<Venue> search(BoundingBox boundingBox, List<Category> categories) {
        Marker center = GeoEarthMathUtils.center(boundingBox);
        int radius = (int) GeoEarthMathUtils.halfLength(boundingBox.getSouthWest(), boundingBox.getNorthEast());
        try {
            return apiCall(center, radius, categories);
        } catch (RuntimeException ignored) {
            log.info("API call failed... Sleep for {} milliseconds before request retry...", googleConfigurationProperties.getCallFailDelay());
            try {
                TimeUnit.MILLISECONDS.sleep(googleConfigurationProperties.getCallFailDelay());
            } catch (InterruptedException e) {
                log.warn("Thread sleep between foursquare API calls was interrupted");
            }
            try {
                return apiCall(center, radius, categories);
            } catch (RuntimeException e) {
                log.error("Error during google API call, message {}", e.getMessage());
                return Collections.emptyList();
            }
        }
    }

    @Override
    public List<Venue> mine(BoundingBox boundingBox, List<Category> categories) {
        return search(boundingBox, categories);
    }

    @Override
    public boolean isReachTheLimit(int venues) {
        return venues >= googleConfigurationProperties.getVenueLimit();
    }

    @Override
    public List<Venue> venueValidation(BoundingBox venuesBoundingBox, List<Venue> venues) {
        double average = venues.stream().mapToDouble(Venue::getRating).average().orElse(0.0);
        return venues.stream()
                .filter(venue -> Objects.nonNull(venue.getCategory()))
                .filter(venue -> Character.isUpperCase(venue.getTitle().charAt(0)))
                .filter(venue -> venue.getRating() > average * 0.1)
                .filter(venue -> GeoEarthMathUtils.contains(venuesBoundingBox, venue.getLocation()))
                .collect(Collectors.toList());
    }

}
