package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;

import com.google.maps.errors.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.client.GoogleClient;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Category;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Pavel Asadchiy
 * on 14:09 24.03.18.
 */
@Slf4j
@Service
public class GoogleService {

    private static final String CATEGORIES_SEPARATOR = "|";

    private final GoogleClient googleClient;

    @Autowired
    public GoogleService(GoogleClient googleClient) {
        this.googleClient = googleClient;
    }

    public List<Venue> apiCall(Marker center, int radius, List<Category> categories) {
        try {
            String googleApiCategories = googleApiUnderstandableCategories(categories);
            return googleClient.apiCall(center, radius, googleApiCategories);
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




}
