package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.configuration.properties.VenueCategoryConfigurationProperties;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Category;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Pavel Asadchiy
 * on 17:50 05.04.18.
 */
@Slf4j
@Service
public class CategoryService {

    private final VenueCategoryConfigurationProperties venueCategoryConfigurationProperties;

    @Autowired
    public CategoryService(VenueCategoryConfigurationProperties venueCategoryConfigurationProperties) {
        this.venueCategoryConfigurationProperties = venueCategoryConfigurationProperties;
    }

    Optional<Category> valueOfByGoogleKey(String key) {
        return venueCategoryConfigurationProperties.getCategories().stream()
                .filter(category -> category.getGoogleKeys().contains(key))
                .findFirst();
    }

    Optional<Category> valueOfByFoursquareKey(String key) {
        return venueCategoryConfigurationProperties.getCategories().stream()
                .filter(category -> category.getFoursquareKeys().contains(key))
                .findFirst();
    }

    Optional<Category> valueOfByKey(String key) {
        return venueCategoryConfigurationProperties.getCategories().stream()
                .filter(category -> category.getTitle().contains(key))
                .findFirst();
    }

    String googleApiCategories(String categories) {
        return map(unJoin(categories)).stream()
                .flatMap(category -> category.getGoogleKeys().stream())
                .collect(Collectors.joining("|")); // Google separator
    }

    String googleApiType(String bboxCategory) { // Contract like [Nature: park]
        List<String> parts = unJoin(bboxCategory);
        if (parts.size() > 1) {
            log.warn("Google support only one type, following first category will be ignoring");
        }
        return parts.get(0).split(":\\s+")[1];
    }

    String foursquareApiCategories(String categories) {
        return map(unJoin(categories)).stream()
                .flatMap(category -> category.getFoursquareKeys().stream())
                .collect(Collectors.joining(",")); // Foursquare separator
    }

    public List<String> getTotalCategories() {
        return venueCategoryConfigurationProperties.getCategories().stream()
                .map(Category::getTitle)
                .collect(Collectors.toList());
    }

    public List<Category> map(List<String> categories) {
        return venueCategoryConfigurationProperties.getCategories().stream()
                .filter(category -> categories.contains(category.getTitle()))
                .collect(Collectors.toList());
    }

    public List<String> getCategoryTypes(Category category, Source source) {
        if (Source.FOURSQUARE == source) {
            return category.getFoursquareKeys();
        }
        if (Source.GOOGLE == source) {
            return category.getGoogleKeys();
        }
        throw new IllegalArgumentException("Incorrect source value = " + source);
    }


    String join(List<String> categories) {
        return categories.stream().collect(Collectors.joining(", ", "[", "]"));
    }

    public List<String> unJoin(String categories) {
        return Arrays.stream(categories.substring(1, categories.length() - 1).split(",\\s+"))
                .map(String::trim)
                .collect(Collectors.toList());
    }

}
