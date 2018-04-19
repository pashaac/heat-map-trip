package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;

import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlacesSearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.client.GoogleClient;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.configuration.properties.GoogleConfigurationProperties;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils.GeoEarthMathUtils;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils.VenueUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Pavel Asadchiy
 * on 14:09 24.03.18.
 */
@Slf4j
@Service
public class GoogleService implements VenueMiner {

    private final GoogleClient googleClient;
    private final GoogleConfigurationProperties googleConfigurationProperties;
    private final CategoryService categoryService;

    @Autowired
    public GoogleService(GoogleClient googleClient, GoogleConfigurationProperties googleConfigurationProperties, CategoryService categoryService) {
        this.googleClient = googleClient;
        this.googleConfigurationProperties = googleConfigurationProperties;
        this.categoryService = categoryService;
    }

    GeocodingResult[] reverseGeocode(Marker location) {
        try {
            return googleClient.reverseGeocode(new LatLng(location.getLatitude(), location.getLongitude()));
        } catch (ApiException | InterruptedException | IOException e) {
            throw new RuntimeException(String.format("Error was happened due to geocoding by coordinates (%s, %s)", location.getLongitude(), location.getLongitude()), e);
        }
    }

    GeocodingResult[] geocode(String address) {
        try {
            return googleClient.geocode(address);
        } catch (ApiException | InterruptedException | IOException e) {
            throw new RuntimeException(String.format("Error was happened due to geocoding by address: %s", address), e);
        }
    }

    /**
     * @param boundingBox - area search
     * @return venues in boundingbox (could return and outside venues)
     */
    @Override
    public List<Venue> apiCall(BoundingBox boundingBox) {
        try {
            if (StringUtils.isEmpty(boundingBox.getCategories())) {
                log.warn("Empty categories array is incorrect, skip it");
                return Collections.emptyList();
            }
            Marker center = GeoEarthMathUtils.center(boundingBox);
            int radius = GeoEarthMathUtils.outerRadius(boundingBox);
            String googleApiCategories = categoryService.googleApiCategories(boundingBox.getCategories());
            List<PlacesSearchResult> placesSearchResults = googleClient.apiCall(center, radius, googleApiCategories);
            log.info("Google API call return {} venues according to categories: {}", placesSearchResults.size(), boundingBox.getCategories());
            Map<String, Set<String>> venueTypes = new HashMap<>();
            return placesSearchResults.stream()
                    .map(venue -> {

                        Venue gVenue = new Venue();
                        gVenue.setTitle(VenueUtils.quotation(venue.name));
                        gVenue.setLocation(new Marker(venue.geometry.location.lat, venue.geometry.location.lng));
                        gVenue.setRating(venue.rating);
                        gVenue.setDescription(String.format("Contact info:\n"
                                        + "\t - Id: %s\n"
                                        + "\t - Icon: %s\n"
                                        + "\t - Types: %s\n"
                                        + "\t - Address: %s\n"
                                        + "Statistic info:\n"
                                        + "\tRating: %s",
                                venue.placeId, venue.icon, Arrays.toString(venue.types), venue.vicinity, venue.rating));

                        List<String> validTypes = Arrays.stream(venue.types)
                                .filter(Objects::nonNull)
                                .filter(googleApiCategories::contains)
                                .collect(Collectors.toList());

                        for (String validType : validTypes) {
                            categoryService.valueOfByGoogleKey(validType).ifPresent(category -> {
                                venueTypes.putIfAbsent(gVenue.getTitle(), new HashSet<>());
                                Set<String> types = venueTypes.get(gVenue.getTitle());
                                if (!types.contains(validType)) {
                                    types.add(validType);
                                    gVenue.setCategory(category.getTitle());
                                    gVenue.setType(validType);
                                }
                            });
                            if (!StringUtils.isEmpty(gVenue.getType())) {
                                break;
                            }
                        }

                        gVenue.setBoundingBox(boundingBox);

                        if (log.isDebugEnabled()) {
                            String debugCategoriesStr = Arrays.stream(venue.types)
                                    .collect(Collectors.joining("|", "[", "]"));
                            log.debug("Venue: {}, {}, rating {}, category: {}", gVenue.getTitle(), debugCategoriesStr, gVenue.getRating(), gVenue.getCategory());
                        }
                        return gVenue;
                    })
                    .collect(Collectors.toList());
        } catch (InterruptedException | ApiException | IOException e) {
            log.error("Google API service temporary unavailable or reject call, message {}", e.getMessage());
            throw new RuntimeException("Google API service temporary unavailable");
        }
    }

    @Override
    public Optional<List<Venue>> apiMine(BoundingBox boundingBox) {
        try {
            return Optional.of(apiCall(boundingBox));
        } catch (RuntimeException ignored) {
            log.info("API call failed... Sleep for {} milliseconds before request retry...", googleConfigurationProperties.getCallFailDelay());
            try {
                TimeUnit.MILLISECONDS.sleep(googleConfigurationProperties.getCallFailDelay());
            } catch (InterruptedException e) {
                log.warn("Thread sleep between google API calls was interrupted");
            }
            try {
                return Optional.of(apiCall(boundingBox));
            } catch (RuntimeException e) {
                log.error("Error during google API call, message {}", e.getMessage());
                return Optional.empty();
            }
        }
    }

    @Override
    public boolean isReachTheLimit(List<Venue> venues) {
        return venues.size() >= googleConfigurationProperties.getVenueLimit();
    }

}
