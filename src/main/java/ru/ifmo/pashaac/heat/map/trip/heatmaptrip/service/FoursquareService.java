package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;

import fi.foyt.foursquare.api.FoursquareApiException;
import fi.foyt.foursquare.api.entities.Category;
import fi.foyt.foursquare.api.entities.CompactVenue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.client.FoursquareClient;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.configuration.properties.FoursquareConfigurationProperties;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils.GeoEarthMathUtils;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils.VenueUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by Pavel Asadchiy
 * on 10:40 05.04.18.
 */
@Slf4j
@Service
public class FoursquareService implements VenueMiner {

    private final FoursquareClient foursquareClient;
    private final FoursquareConfigurationProperties foursquareConfigurationProperties;
    private final CategoryService categoryService;

    public FoursquareService(FoursquareClient foursquareClient, FoursquareConfigurationProperties foursquareConfigurationProperties, CategoryService categoryService) {
        this.foursquareClient = foursquareClient;
        this.foursquareConfigurationProperties = foursquareConfigurationProperties;
        this.categoryService = categoryService;
    }

    @Override
    public List<Venue> apiCall(BoundingBox boundingBox) {
        try {
            if (StringUtils.isEmpty(boundingBox.getCategories())) {
                log.warn("Empty categories array is incorrect, skip it");
                return Collections.emptyList();
            }
            String foursquareApiCategories = categoryService.foursquareApiCategories(boundingBox.getCategories());
            GeoEarthMathUtils.center(boundingBox);
            List<CompactVenue> compactVenues = foursquareClient.apiCall(GeoEarthMathUtils.center(boundingBox), GeoEarthMathUtils.outerRadius(boundingBox), foursquareConfigurationProperties.getVenueLimit(), foursquareApiCategories);
            log.info("Foursquare API call return {} venues according to categories: {}", compactVenues.size(), boundingBox.getCategories());
            Map<String, Set<String>> venueTypes = new HashMap<>();
            return compactVenues.stream()
                    .map(venue -> {
                        Venue fVenue = new Venue();
                        fVenue.setTitle(VenueUtils.quotation(venue.getName()));
                        fVenue.setLocation(new Marker(venue.getLocation().getLat(), venue.getLocation().getLng()));
                        fVenue.setRating(venue.getStats().getCheckinsCount());
                        String address = String.format("%s, %s, %s", venue.getLocation().getCountry(), venue.getLocation().getCity(), venue.getLocation().getAddress());
                        fVenue.setDescription(String.format("Contact info:\n"
                                        + "\t - Phone: %s\n"
                                        + "\t - E-mail: %s\n"
                                        + "\t - Twitter: %s\n"
                                        + "\t - Facebook: %s\n"
                                        + "\t - Id: %s\n"
                                        + "\t - Address: %s\n"
                                        + "URL: %s\n"
                                        + "Statistic info:\n"
                                        + "\t - Rating: %s\n"
                                        + "\t - Checkins: %s\n"
                                        + "\t - Users: %s\n"
                                        + "\t - Tip: %s\n",
                                venue.getContact().getFormattedPhone(), venue.getContact().getEmail(), venue.getContact().getTwitter(),
                                venue.getContact().getFacebook(), venue.getId(), address, venue.getUrl(), venue.getRating(), venue.getStats().getCheckinsCount(),
                                venue.getStats().getUsersCount(), venue.getStats().getTipCount()));

                        List<String> validTypes = Arrays.stream(venue.getCategories())
                                .filter(Objects::nonNull)
                                .map(Category::getId)
                                .filter(foursquareApiCategories::contains)
                                .collect(Collectors.toList());


                        for (String validType : validTypes) {
                            categoryService.valueOfByFoursquareKey(validType).ifPresent(category -> {
                                venueTypes.putIfAbsent(fVenue.getTitle(), new HashSet<>());
                                Set<String> types = venueTypes.get(fVenue.getTitle());
                                if (!types.contains(validType)) {
                                    types.add(validType);
                                    fVenue.setCategory(category.getTitle());
                                    fVenue.setType(validType);
                                }
                            });
                            if (!StringUtils.isEmpty(fVenue.getType())) {
                                break;
                            }
                        }

                        fVenue.setBoundingBox(boundingBox);

                        if (log.isDebugEnabled()) {
                            String debugCategoriesStr = Arrays.stream(venue.getCategories())
                                    .map(category -> category.getName() + " - " + category.getId())
                                    .collect(Collectors.joining("|", "[", "]"));
                            log.debug("Venue: {}, {}, rating {}, category: {}", fVenue.getTitle(), debugCategoriesStr, fVenue.getRating(), fVenue.getCategory());
                        }

                        return fVenue;
                    })
                    .collect(Collectors.toList());
        } catch (FoursquareApiException e) {
            log.error("Foursquare API service temporary unavailable or reject call, message {}", e.getMessage());
            throw new RuntimeException("Foursquare API service temporary unavailable");
        }
    }

    @Override
    public Optional<List<Venue>> apiMine(BoundingBox boundingBox) {
        try {
            return Optional.of(apiCall(boundingBox));
        } catch (RuntimeException ignored) {
            log.warn("API call failed... Sleep for {} milliseconds before request retry...", foursquareConfigurationProperties.getCallFailDelay());
            try {
                TimeUnit.MILLISECONDS.sleep(foursquareConfigurationProperties.getCallFailDelay());
            } catch (InterruptedException e) {
                log.warn("Thread sleep between foursquare API calls was interrupted");
            }
            try {
                return Optional.of(apiCall(boundingBox));
            } catch (RuntimeException e) {
                log.error("Error during foursquare API call, message {}", e.getMessage());
                return Optional.empty();
            }
        }
    }

    @Override
    public boolean isReachTheLimit(List<Venue> venues) {
        return venues.size() >= foursquareConfigurationProperties.getVenueLimit();
    }

}
