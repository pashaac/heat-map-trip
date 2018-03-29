package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.client;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlacesSearchResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Category;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils.VenueUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Pavel Asadchiy
 * on 12:47 24.03.18.
 */
@Slf4j
@Component
public class GoogleClient {

    private final GeoApiContext googleGeoApiContext;

    @Autowired
    public GoogleClient(GeoApiContext googleGeoApiContext) {
        this.googleGeoApiContext = googleGeoApiContext;
    }

    @SneakyThrows(value = {InterruptedException.class, ApiException.class, IOException.class})
    public GeocodingResult[] reverseGeocode(LatLng location) {
        return GeocodingApi.reverseGeocode(googleGeoApiContext, location).await();
    }

    @SneakyThrows(value = {InterruptedException.class, ApiException.class, IOException.class})
    public GeocodingResult[] geocode(String address) {
        return GeocodingApi.geocode(googleGeoApiContext, address).await();
    }

    private PlacesSearchResponse nearbySearchCall(Marker center, int radius, String categories) throws InterruptedException, ApiException, IOException {
        return PlacesApi.nearbySearchQuery(googleGeoApiContext, new LatLng(center.getLatitude(), center.getLongitude()))
                .radius(radius)
                .language("ru")
                .custom("type", categories)
                .await();
    }

    public List<Venue> apiCall(Marker center, int radius, String categories) throws InterruptedException, ApiException, IOException {
        PlacesSearchResponse searchResponse = nearbySearchCall(center, radius, categories);

        return Arrays.stream(searchResponse.results)
                .map(venue -> {

                    Venue gVenue = new Venue();
                    gVenue.setTitle(VenueUtils.quotation(venue.name));
                    gVenue.setLocation(new Marker(venue.geometry.location.lat, venue.geometry.location.lng));
                    gVenue.setSource(Source.GOOGLE);
                    gVenue.setRating(venue.rating);
                    gVenue.setAddress(venue.vicinity);
                    gVenue.setDescription(String.format("Contact info:\n"
                                    + "\t - Id: %s\n"
                                    + "\t - Icon: %s\n"
                                    + "\t - Types: %s\n"
                                    + "Statistic info:\n"
                                    + "\tRating: %s",
                            venue.placeId, venue.icon, Arrays.toString(venue.types), venue.rating));

                    for (String type : venue.types) {
                        Optional<Category> venueCategory = Category.valueOfGoogleCategory(type);
                        if (venueCategory.isPresent()) {
                            gVenue.setCategory(venueCategory.get());
                            break;
                        }
                    }

                    if (log.isDebugEnabled()) {
                        String debugCategoriesStr = Arrays.stream(venue.types)
                                .collect(Collectors.joining("|", "[", "]"));
                        log.debug("Venue: {}, {}, rating {}, category: {}", gVenue.getTitle(), debugCategoriesStr, gVenue.getRating(), gVenue.getCategory());
                    }
                    return gVenue;
                })
                .collect(Collectors.toList());
    }

}
