package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Category;
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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"southWestLatitude", "southWestLongitude", "northEastLatitude", "northEastLongitude", "source", "category", "type"}))
public class BoundingBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "southWestLatitude", precision = 7)),
            @AttributeOverride(name = "longitude", column = @Column(name = "southWestLongitude", precision = 7)),
    })
    private Marker southWest;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "northEastLatitude", precision = 7)),
            @AttributeOverride(name = "longitude", column = @Column(name = "northEastLongitude", precision = 7)),
    })
    private Marker northEast;

    @Enumerated(EnumType.STRING)
    private Source source;

    private String category;

    private String type;

    private boolean valid;

    @JsonBackReference("city-boundingBoxes")
    @ManyToOne(targetEntity = City.class, fetch = FetchType.EAGER)
    private City city;

    @JsonManagedReference("boundingBox-venues")
    @OneToMany(targetEntity = Venue.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "boundingBox")
    private List<Venue> venues = new ArrayList<>();


    public BoundingBox(City city, Source source, Category category, String type) {
        this.southWest = city.getSouthWest();
        this.northEast = city.getNorthEast();
        this.source = source;
        this.category = category.getTitle();
        this.type = type;
        this.city = city;
    }

    public BoundingBox(BoundingBox boundingBox, Source source, Category category, String type) {
        this.southWest = boundingBox.getSouthWest();
        this.northEast = boundingBox.getNorthEast();
        this.source = source;
        this.category = category.getTitle();
        this.type = type;
        this.city = boundingBox.getCity();
    }

    public BoundingBox(Marker southWest, Marker northEast, Source source, String category, String type, City city) {
        this.southWest = southWest;
        this.northEast = northEast;
        this.source = source;
        this.category = category;
        this.type = type;
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
