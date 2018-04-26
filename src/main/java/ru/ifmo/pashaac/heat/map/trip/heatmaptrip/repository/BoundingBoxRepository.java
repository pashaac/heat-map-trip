package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.BoundingBox;

import java.util.List;

@Transactional
@Repository
public interface BoundingBoxRepository extends JpaRepository<BoundingBox, Long>  {
    List<BoundingBox> findBoundingBoxesByCity_IdAndSourceAndCategoriesContainsAndValid(Long cityId, Source source, String category, boolean valid);
    List<BoundingBox> findBoundingBoxesByValidIsFalse();
}
