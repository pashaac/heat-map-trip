package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.configuration;

import com.google.maps.GeoApiContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.configuration.properties.GoogleConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * Created by Pavel Asadchiy
 * on 12:48 24.03.18.
 */
@Configuration
public class GoogleConfiguration {

    private final GoogleConfigurationProperties googleConfigurationProperties;

    @Autowired
    public GoogleConfiguration(GoogleConfigurationProperties googleConfigurationProperties) {
        this.googleConfigurationProperties = googleConfigurationProperties;
    }

    @Bean
    public GeoApiContext googleGeoApiContext() {
        return new GeoApiContext.Builder()
                .apiKey(googleConfigurationProperties.getKey())
                .queryRateLimit(googleConfigurationProperties.getRequestsLimit())
                .readTimeout(googleConfigurationProperties.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(googleConfigurationProperties.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .connectTimeout(googleConfigurationProperties.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .build();
    }

}
