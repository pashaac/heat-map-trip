package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;

import javax.persistence.*;

/**
 * Created by Pavel Asadchiy
 * on 13:24 24.03.18.
 */
@Getter
@Setter
@NoArgsConstructor

@Entity
@Table
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "latitude", precision = 7)),
            @AttributeOverride(name = "longitude", column = @Column(name = "longitude", precision = 7)),
    })
    private Marker location;

    private double rating;

    @Column(length = 1024)
    private String description;

    private boolean valid;

    @JsonBackReference("boundingBox-venues")
    @ManyToOne(targetEntity = BoundingBox.class, fetch = FetchType.EAGER)
    private BoundingBox boundingBox;

}
