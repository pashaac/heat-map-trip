package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Source;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pavel Asadchiy
 * on 13:37 24.03.18.
 */
@Getter
@Setter
@NoArgsConstructor

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"southWestLatitude", "southWestLongitude",
        "northEastLatitude", "northEastLongitude", "source", "searchKey"}))
public class BoundingBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Enumerated(EnumType.STRING)
    private Source source;

    @Column(length = 1024)
    private String searchKey; // joined list of categories / should be empty only for city boundingBox

    private boolean collect;

    @JsonBackReference("city-boundingBoxes")
    @ManyToOne(targetEntity = City.class, fetch = FetchType.EAGER)
    private City city;

    @JsonManagedReference("boundingBox-venues")
    @OneToMany(targetEntity = Venue.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "boundingBox")
    private List<Venue> venues = new ArrayList<>();

    public BoundingBox(Marker southWest, Marker northEast, Source source, String searchKey, City city) {
        this.southWest = southWest;
        this.northEast = northEast;
        this.source = source;
        this.searchKey = searchKey;
        this.city = city;
    }

    public BoundingBox(Marker southWest, Marker northEast, City city) {
        this.southWest = southWest;
        this.northEast = northEast;
        this.city = city;
    }

    public BoundingBox(Marker southWest, Marker northEast) {
        this.southWest = southWest;
        this.northEast = northEast;
    }
}
