package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Category;

import java.util.List;

/**
 * Created by Pavel Asadchiy
 * on 13:29 24.03.18.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "venue")
@EnableConfigurationProperties
public class VenueCategoryConfigurationProperties {

    private List<Category> categories;
    private Double lowerRatingBound;
    private Integer distributionArea;
    private Integer distributionCount;
    private Integer distributionIntersectionDistance;

}
