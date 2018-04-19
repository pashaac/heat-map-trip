package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pavel Asadchiy
 * on 23:06 29.03.18.
 */
@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"city", "country"}))
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;
    private String country;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "southWestLatitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "southWestLongitude")),
    })
    private Marker southWest;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "northEastLatitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "northEastLongitude")),
    })
    private Marker northEast;

    @JsonManagedReference("city-boundingBoxes")
    @OneToMany(targetEntity = BoundingBox.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "city")
    private List<BoundingBox> boundingBoxes = new ArrayList<>();

    public City(String city, String country, BoundingBox boundingBox) {
        this.city = city;
        this.country = country;
        this.southWest = boundingBox.getSouthWest();
        this.northEast = boundingBox.getNorthEast();
    }

    public BoundingBox getBoundingBox() {
        return new BoundingBox(southWest, northEast, this);
    }
}
