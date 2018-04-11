package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.BoundingBox;

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

    @JsonManagedReference("city-venue")
    @OneToMany(targetEntity = Venue.class, cascade = CascadeType.REMOVE, mappedBy = "city")
    private List<Venue> venues = new ArrayList<>();

    public City(String city, String country, BoundingBox boundingBox) {
        this.city = city;
        this.country = country;
        this.boundingBox = boundingBox;
    }
}
