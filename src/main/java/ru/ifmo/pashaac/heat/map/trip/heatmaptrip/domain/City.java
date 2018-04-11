package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.BoundingBox;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Pavel Asadchiy
 * on 23:06 29.03.18.
 */
@Getter
@Setter
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"city", "country"}))
@Entity
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;
    private String country;

    @AttributeOverrides({
            @AttributeOverride(name = "southWest.latitude", column = @Column(name = "southWestLatitude")),
            @AttributeOverride(name = "southWest.longitude", column = @Column(name = "southWestLongitude")),
            @AttributeOverride(name = "northEast.latitude", column = @Column(name = "northEastLatitude")),
            @AttributeOverride(name = "northEast.longitude", column = @Column(name = "northEastLongitude"))
    })
    private BoundingBox boundingBox;

    private String status;
    @ElementCollection
    private Set<String> categories = new HashSet<>();

    public City(String city, String country, BoundingBox boundingBox) {
        this.city = city;
        this.country = country;
        this.boundingBox = boundingBox;
    }
}
