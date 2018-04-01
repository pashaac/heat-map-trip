package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils;

import com.grum.geocalc.DegreeCoordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.BoundingBox;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;

import java.util.Arrays;
import java.util.List;

/**
 * Util class for geo calculations on the Earth with geocalc.jap library help
 *
 * @author Pavel Asadchiy
 */
public class GeoEarthMathUtils {

    public static double distance(Marker point1, Marker point2) {
        return EarthCalc.getVincentyDistance(convert(point1), convert(point2));
    }

    private static Point convert(Marker marker) {
        return new Point(new DegreeCoordinate(marker.getLatitude()), new DegreeCoordinate(marker.getLongitude()));
    }

    private static Marker median(Marker point1, Marker point2) {
        double bearing = EarthCalc.getBearing(convert(point1), convert(point2));
        double distance = distance(point1, point2);
        Point median = EarthCalc.pointRadialDistance(convert(point1), bearing, 0.5 * distance);
        return new Marker(median.getLatitude(), median.getLongitude());
    }

    public static double halfLength(Marker point1, Marker point2) {
        Marker median = median(point1, point2);
        return 0.5 * (distance(point1, median) + distance(point2, median));
    }

    public static Marker center(BoundingBox boundingBox) {
        return median(boundingBox.getSouthWest(), boundingBox.getNorthEast());
    }

    private static Marker boundingBoxCenter(BoundingBox box) {
        return median(box.getSouthWest(), box.getNorthEast());
    }

    public static BoundingBox leftUpBoundingBox(BoundingBox box) {
        return new BoundingBox(getNorthWest(leftDownBoundingBox(box)), getNorthWest(rightUpBoundingBox(box)));
    }

    public static BoundingBox leftDownBoundingBox(BoundingBox box) {
        Marker center = boundingBoxCenter(box);
        return new BoundingBox(box.getSouthWest(), center);
    }

    public static BoundingBox rightUpBoundingBox(BoundingBox box) {
        Marker center = boundingBoxCenter(box);
        return new BoundingBox(center, box.getNorthEast());
    }

    public static BoundingBox rightDownBoundingBox(BoundingBox box) {
        return new BoundingBox(getSouthEast(leftDownBoundingBox(box)), getSouthEast(rightUpBoundingBox(box)));
    }

    public static List<BoundingBox> getQuarters(BoundingBox box) {
        return Arrays.asList(leftUpBoundingBox(box), rightUpBoundingBox(box), rightDownBoundingBox(box), leftDownBoundingBox(box));
    }


    private static Marker getNorthWest(BoundingBox boundingBox) {
        return new Marker(boundingBox.getNorthEast().getLatitude(), boundingBox.getSouthWest().getLongitude());
    }

    private static Marker getSouthEast(BoundingBox boundingBox) {
        return new Marker(boundingBox.getSouthWest().getLatitude(), boundingBox.getNorthEast().getLongitude());
    }

    public static boolean contains(BoundingBox boundingBox, Marker point) {
        boolean latitudeIntersection = boundingBox.getSouthWest().getLatitude() < point.getLatitude() && point.getLatitude() < boundingBox.getNorthEast().getLatitude();
        boolean longitudeIntersection = boundingBox.getSouthWest().getLongitude() < point.getLongitude() && point.getLongitude() < boundingBox.getNorthEast().getLongitude();
        return latitudeIntersection && longitudeIntersection;
    }

}
