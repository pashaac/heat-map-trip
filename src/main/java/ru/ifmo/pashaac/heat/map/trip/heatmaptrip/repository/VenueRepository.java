package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.Venue;

/**
 * Created by Pavel Asadchiy
 * on 22:34 11.04.18.
 */
@Transactional
@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
}
