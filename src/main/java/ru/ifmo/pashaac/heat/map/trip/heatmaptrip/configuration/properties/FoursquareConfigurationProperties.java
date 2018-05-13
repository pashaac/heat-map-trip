package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Pavel Asadchiy
 * on 10:31 05.04.18.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "foursquare.api")
@EnableConfigurationProperties
public class FoursquareConfigurationProperties {

    private String clientId;
    private String clientSecret;
    private String redirectUrl;

    private Integer venueLimit;
    private Integer venueLimitMax;
    private Integer callFailDelay;

}
