package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.configuration.properties.VenueCategoryConfigurationProperties;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Category;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Pavel Asadchiy
 * on 17:50 05.04.18.
 */
@Service
public class CategoryService {

    private final VenueCategoryConfigurationProperties venueCategoryConfigurationProperties;

    @Autowired
    public CategoryService(VenueCategoryConfigurationProperties venueCategoryConfigurationProperties) {
        this.venueCategoryConfigurationProperties = venueCategoryConfigurationProperties;
    }

    public Optional<Category> valueOfByGoogleKey(String key) {
        return venueCategoryConfigurationProperties.getCategories().stream()
                .filter(category -> category.getGoogleKeys().contains(key))
                .findFirst();
    }

    public Optional<Category> valueOfByFoursquareKey(String key) {
        return venueCategoryConfigurationProperties.getCategories().stream()
                .filter(category -> category.getFoursquareKeys().contains(key))
                .findFirst();
    }

    public String googleApiCategories(String categories) {
        return map(unjoin(categories)).stream()
                .flatMap(category -> category.getGoogleKeys().stream())
                .collect(Collectors.joining("|")); // Google separator
    }

    public String foursquareApiCategories(String categories) {
        return map(unjoin(categories)).stream()
                .flatMap(category -> category.getFoursquareKeys().stream())
                .collect(Collectors.joining(",")); // Foursquare separator
    }

    public List<String> getVenueCategories() {
        return venueCategoryConfigurationProperties.getCategories().stream()
                .map(Category::getTitle)
                .collect(Collectors.toList());
    }

    public List<String> unmap(List<Category> categories) {
        return categories.stream()
                .map(Category::getTitle)
                .collect(Collectors.toList());
    }

    public List<Category> map(List<String> categories) {
        return venueCategoryConfigurationProperties.getCategories().stream()
                .filter(category -> categories.contains(category.getTitle()))
                .collect(Collectors.toList());
    }

    public String join(List<String> categories) {
        return categories.stream().collect(Collectors.joining(", ", "[", "]"));
    }

    public List<String> unjoin(String categories) {
        return Arrays.stream(categories.substring(1, categories.length() - 1).split(", "))
                .map(String::trim)
                .collect(Collectors.toList());
    }

}
