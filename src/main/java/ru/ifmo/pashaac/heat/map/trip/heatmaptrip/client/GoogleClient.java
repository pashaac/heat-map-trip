package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.client;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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

    public GeocodingResult[] reverseGeocode(LatLng location) throws InterruptedException, ApiException, IOException {
        return GeocodingApi.reverseGeocode(googleGeoApiContext, location).await();
    }

    public GeocodingResult[] geocode(String address) throws InterruptedException, ApiException, IOException {
        return GeocodingApi.geocode(googleGeoApiContext, address).await();
    }

    private PlacesSearchResponse nearbySearchCall(Marker center, int radius, String categories) throws InterruptedException, ApiException, IOException {
        return PlacesApi.nearbySearchQuery(googleGeoApiContext, new LatLng(center.getLatitude(), center.getLongitude()))
                .radius(radius)
                .language("ru")
                .custom("types", categories)
                .await();
    }

    public List<PlacesSearchResult> apiCall(Marker center, int radius, String categories) throws InterruptedException, ApiException, IOException {
        PlacesSearchResponse searchResponse = nearbySearchCall(center, radius, categories);
        return Arrays.asList(searchResponse.results);
    }

}
