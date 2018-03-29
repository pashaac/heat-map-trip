package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by Pavel Asadchiy
 * on 13:46 24.03.18.
 */
@EnableSwagger2
@Configuration
public class SwaggerConfiguration {

    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .groupName("Heat-Map-Trip")
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("ru.ifmo.pashaac.heat.map.trip.heatmaptrip.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Heat-Map-Trip")
                .description("Geo worldwide service to visualise / analyze tourist attractions / venues impact")
                .contact(new Contact("Pavel Asadchiy", "https://vk.com/pasha_ac", "pavel.asadchiy@gmail.com"))
                .version("1.0")
                .build();
    }
}
