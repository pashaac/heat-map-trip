package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service.BoundingBoxService;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.service.VenueService;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class InvalidBoundingBoxScheduler {

    private final BoundingBoxService boundingBoxService;
    private final VenueService venueService;

    @Autowired
    public InvalidBoundingBoxScheduler(BoundingBoxService boundingBoxService, VenueService venueService) {
        this.boundingBoxService = boundingBoxService;
        this.venueService = venueService;
    }

    @Scheduled(fixedRate = 1000 * 60 * 90)
    public void schedulerInvalidBoundingBoxes() {
        log.info("Scheduler wake up to check unhandled invalid bounding boxes...");
        List<BoundingBox> invalidBoundingBoxes = boundingBoxService.getTotalInvalidBoundingBoxes();
        if (CollectionUtils.isEmpty(invalidBoundingBoxes)) {
            log.info("Good news! No invalid bounding boxes :)");
            return;
        }
        Collections.shuffle(invalidBoundingBoxes);
        venueService.quadTreeMine(invalidBoundingBoxes);
    }
}
