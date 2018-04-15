package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
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
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"title", "category", "rating"}))
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

    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "longitude")),
    })
    private Marker location;
    private String address;

    private double rating;

    @JsonBackReference("city-venue")
    @ManyToOne(targetEntity = City.class)
    private City city;


}
