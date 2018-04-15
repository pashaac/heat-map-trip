package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;

import java.util.List;

/**
 * Created by Pavel Asadchiy
 * on 22:34 11.04.18.
 */
@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    List<Venue> findVenuesByCity_IdAndSourceAndCategory(Long cityId, Source source, String category);
}
