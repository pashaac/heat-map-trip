package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils;

import com.grum.geocalc.DegreeCoordinate;
import com.grum.geocalc.EarthCalc;
import com.grum.geocalc.Point;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data.Marker;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.domain.BoundingBox;

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
        return measureOut(point1, point2, 0.5);
//        double bearing = EarthCalc.getBearing(convert(point1), convert(point2));
//        double distance = distance(point1, point2);
//        Point median = EarthCalc.pointRadialDistance(convert(point1), bearing, 0.5 * distance);
//        return new Marker(median.getLatitude(), median.getLongitude());
    }

    public static Marker measureOut(Marker point1, Marker point2, double ratio) {
        double bearing = EarthCalc.getBearing(convert(point1), convert(point2));
        double distance = distance(point1, point2);
        Point point = EarthCalc.pointRadialDistance(convert(point1), bearing, ratio * distance);
        return new Marker(point.getLatitude(), point.getLongitude());
    }

    public static boolean contains(BoundingBox boundingBox, Marker point) {
        boolean latitudeIntersection = boundingBox.getSouthWest().getLatitude() < point.getLatitude() && point.getLatitude() < boundingBox.getNorthEast().getLatitude();
        boolean longitudeIntersection = boundingBox.getSouthWest().getLongitude() < point.getLongitude() && point.getLongitude() < boundingBox.getNorthEast().getLongitude();
        return latitudeIntersection && longitudeIntersection;
    }

    public static Marker center(BoundingBox box) {
        return GeoEarthMathUtils.median(box.getSouthWest(), box.getNorthEast());
    }

    public static Marker getNorthWest(BoundingBox boundingBox) {
        return new Marker(boundingBox.getNorthEast().getLatitude(), boundingBox.getSouthWest().getLongitude());
    }

    public static Marker getSouthEast(BoundingBox boundingBox) {
        return new Marker(boundingBox.getSouthWest().getLatitude(), boundingBox.getNorthEast().getLongitude());
    }

    private static BoundingBox leftUpBoundingBox(BoundingBox box) {
        return new BoundingBox(getNorthWest(leftDownBoundingBox(box)), getNorthWest(rightUpBoundingBox(box)), box.getSource(), box.getCategories(), box.getCity());
    }

    private static BoundingBox leftDownBoundingBox(BoundingBox box) {
        Marker center = center(box);
        return new BoundingBox(box.getSouthWest(), center, box.getSource(), box.getCategories(), box.getCity());
    }

    private static BoundingBox rightUpBoundingBox(BoundingBox box) {
        Marker center = center(box);
        return new BoundingBox(center, box.getNorthEast(), box.getSource(), box.getCategories(), box.getCity());
    }

    private static BoundingBox rightDownBoundingBox(BoundingBox box) {
        return new BoundingBox(getSouthEast(leftDownBoundingBox(box)), getSouthEast(rightUpBoundingBox(box)), box.getSource(), box.getCategories(), box.getCity());
    }

    public static List<BoundingBox> getQuarters(BoundingBox box) {
        return Arrays.asList(leftUpBoundingBox(box), rightUpBoundingBox(box), rightDownBoundingBox(box), leftDownBoundingBox(box));
    }

    public static int outerRadius(BoundingBox box) {
        Marker median = median(box.getSouthWest(), box.getNorthEast());
        return (int) (0.5 * (distance(box.getSouthWest(), median) + distance(box.getNorthEast(), median)));
    }

}
