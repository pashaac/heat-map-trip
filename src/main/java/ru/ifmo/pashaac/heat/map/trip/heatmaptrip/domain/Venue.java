package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;

import javax.persistence.*;

/**
 * Created by Pavel Asadchiy
 * on 13:24 24.03.18.
 */
@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"title", "latitude", "longitude", "category", "source", "sourceCategory"}))
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1024)
    private String description;

    private String category;

    @Enumerated(EnumType.STRING)
    private Source source;

    private String sourceCategory;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "longitude")),
    })
    private Marker location;
    private String address;

    private double rating;

    private boolean valid;

    @JsonBackReference("boundingBox-venues")
    @ManyToOne(targetEntity = BoundingBox.class, fetch = FetchType.EAGER)
    private BoundingBox boundingBox;

}
