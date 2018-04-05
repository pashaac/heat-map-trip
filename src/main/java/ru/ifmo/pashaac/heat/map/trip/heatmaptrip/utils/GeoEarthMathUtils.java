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

    private static Point convert(Marker marker) {
        return new Point(new DegreeCoordinate(marker.getLatitude()), new DegreeCoordinate(marker.getLongitude()));
    }

    public static double distance(Marker point1, Marker point2) {
        return EarthCalc.getVincentyDistance(convert(point1), convert(point2));
    }

    private static Marker median(Marker point1, Marker point2) {
        double bearing = EarthCalc.getBearing(convert(point1), convert(point2));
        double distance = distance(point1, point2);
        Point median = EarthCalc.pointRadialDistance(convert(point1), bearing, 0.5 * distance);
        return new Marker(median.getLatitude(), median.getLongitude());
    }

    public static boolean contains(BoundingBox boundingBox, Marker point) {
        boolean latitudeIntersection = boundingBox.getSouthWest().getLatitude() < point.getLatitude() && point.getLatitude() < boundingBox.getNorthEast().getLatitude();
        boolean longitudeIntersection = boundingBox.getSouthWest().getLongitude() < point.getLongitude() && point.getLongitude() < boundingBox.getNorthEast().getLongitude();
        return latitudeIntersection && longitudeIntersection;
    }

    public static Marker center(BoundingBox box) {
        return GeoEarthMathUtils.median(box.getSouthWest(), box.getNorthEast());
    }

    private static Marker getNorthWest(BoundingBox boundingBox) {
        return new Marker(boundingBox.getNorthEast().getLatitude(), boundingBox.getSouthWest().getLongitude());
    }

    private static Marker getSouthEast(BoundingBox boundingBox) {
        return new Marker(boundingBox.getSouthWest().getLatitude(), boundingBox.getNorthEast().getLongitude());
    }

    private static BoundingBox leftUpBoundingBox(BoundingBox box) {
        return new BoundingBox(getNorthWest(leftDownBoundingBox(box)), getNorthWest(rightUpBoundingBox(box)));
    }

    private static BoundingBox leftDownBoundingBox(BoundingBox box) {
        Marker center = center(box);
        return new BoundingBox(box.getSouthWest(), center);
    }

    private static BoundingBox rightUpBoundingBox(BoundingBox box) {
        Marker center = center(box);
        return new BoundingBox(center, box.getNorthEast());
    }

    private static BoundingBox rightDownBoundingBox(BoundingBox box) {
        return new BoundingBox(getSouthEast(leftDownBoundingBox(box)), getSouthEast(rightUpBoundingBox(box)));
    }

    public static List<BoundingBox> getQuarters(BoundingBox box) {
        return Arrays.asList(leftUpBoundingBox(box), rightUpBoundingBox(box), rightDownBoundingBox(box), leftDownBoundingBox(box));
    }

    public static int outerRadius(BoundingBox box) {
        Marker median = median(box.getSouthWest(), box.getNorthEast());
        return (int) (0.5 * (distance(box.getSouthWest(), median) + distance(box.getNorthEast(), median)));
    }

}
