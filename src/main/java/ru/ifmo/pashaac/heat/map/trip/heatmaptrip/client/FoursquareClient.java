package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.client;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.FoursquareApiException;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.entities.CompactVenue;
import fi.foyt.foursquare.api.entities.VenuesSearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Pavel Asadchiy
 * on 10:30 05.04.18.
 */
@Slf4j
@Component
public class FoursquareClient {

    public static final String SEARCH_INTENT = "browse";

    private final FoursquareApi foursquareApi;

    @Autowired
    public FoursquareClient(FoursquareApi foursquareApi) {
        this.foursquareApi = foursquareApi;
    }

    /**
     * @throws FoursquareApiException when trouble with foursquare API or internet connection
     */
    public List<CompactVenue> apiCall(Marker center, int radius, int limit, String categories) throws FoursquareApiException {
        Map<String, String> params = new HashMap<>();
        params.put("ll", String.format("%s,%s", center.getLatitude(), center.getLongitude()));
        params.put("radius", String.valueOf(radius));
        params.put("intent", SEARCH_INTENT);
        params.put("categoryId", categories);
        params.put("limit", String.valueOf(limit));
        Result<VenuesSearchResult> venuesSearchResult = foursquareApi.venuesSearch(params);

        if (HttpStatus.valueOf(venuesSearchResult.getMeta().getCode()) != HttpStatus.OK) {
            log.error("Foursquare venues search api call return code {}", venuesSearchResult.getMeta().getCode());
            throw new FoursquareApiException("Foursquare venues search api call return code " + venuesSearchResult.getMeta().getCode());
        }

        return Arrays.asList(venuesSearchResult.getResult().getVenues());
    }

}
