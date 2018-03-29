package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Pavel Asadchiy
 * on 12:59 24.03.18.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "google.api")
@EnableConfigurationProperties
public class GoogleConfigurationProperties {

    private String key;

    private Integer readTimeout;
    private Integer writeTimeout;
    private Integer connectTimeout;
    private Integer requestsLimit;

    private Integer venueLimit;
    private Integer callFailDelay;
}
