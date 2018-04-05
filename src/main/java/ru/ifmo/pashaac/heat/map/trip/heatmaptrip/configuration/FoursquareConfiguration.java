package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.configuration;

import fi.foyt.foursquare.api.FoursquareApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.configuration.properties.FoursquareConfigurationProperties;

/**
 * Created by Pavel Asadchiy
 * on 10:37 05.04.18.
 */
@Configuration
public class FoursquareConfiguration {

    private final FoursquareConfigurationProperties configurationProperties;

    @Autowired
    public FoursquareConfiguration(FoursquareConfigurationProperties foursquareConfigurationProperties) {
        this.configurationProperties = foursquareConfigurationProperties;
    }

    @Bean
    public FoursquareApi foursquareApi() {
        return new FoursquareApi(configurationProperties.getClientId(), configurationProperties.getClientSecret(), configurationProperties.getRedirectUrl());
    }
}
