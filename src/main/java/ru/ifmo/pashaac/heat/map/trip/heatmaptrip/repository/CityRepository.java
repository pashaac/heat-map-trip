package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.City;

/**
 * Created by Pavel Asadchiy
 * on 14:48 11.04.18.
 */
@Repository
public interface CityRepository extends JpaRepository<City, Long>  {
    City findCityByCityAndCountry(String city, String country);
    boolean existsCityByCityAndCountry(String city, String country);
}
