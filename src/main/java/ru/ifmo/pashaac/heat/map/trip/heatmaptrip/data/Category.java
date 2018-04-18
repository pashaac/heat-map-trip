package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Created by Pavel Asadchiy
 * on 13:38 24.03.18.
 */
@Getter
@Setter
@ToString(exclude = {"googleKeys", "foursquareKeys"})
public class Category {

    private String title;
    private List<String> googleKeys;
    private List<String> foursquareKeys;

}