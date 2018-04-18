package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.repository.BoundingBoxRepository;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils.GeoEarthMathUtils;

import java.util.List;

@Service
public class VenueTransactionalService {

    private final BoundingBoxRepository boundingBoxRepository;

    @Autowired
    public VenueTransactionalService(BoundingBoxRepository boundingBoxRepository) {
        this.boundingBoxRepository = boundingBoxRepository;
    }

    @Transactional
    public BoundingBox save(BoundingBox boundingBox) {
        return boundingBoxRepository.save(boundingBox);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<BoundingBox> splitBoundingBox(BoundingBox boundingBox) {
        List<BoundingBox> quarters = GeoEarthMathUtils.getQuarters(boundingBox);
        List<BoundingBox> savedQuarters = boundingBoxRepository.save(quarters);
        boundingBoxRepository.delete(boundingBox);
        return savedQuarters;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BoundingBox saveVenueBoundingBox(BoundingBox boundingBox) {
        boundingBox.setCollect(true);
        return boundingBoxRepository.save(boundingBox);
    }

}
