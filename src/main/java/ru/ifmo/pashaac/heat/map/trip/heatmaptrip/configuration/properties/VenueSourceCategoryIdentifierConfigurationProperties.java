package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.configuration.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Pavel Asadchiy
 * on 13:29 24.03.18.
 */
@Getter
@Configuration
@ConfigurationProperties(prefix = "venue.category")
@EnableConfigurationProperties
public class VenueSourceCategoryIdentifierConfigurationProperties {

    private String googlePark;

}
