package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import ru.ifmo.pashaac.heat.map.trip.heatmaptrip.utils.GeoEarthMathUtils;
import smile.math.distance.Distance;

@Getter
@Setter
@NoArgsConstructor
public class ColorMarker implements Clusterable, DistanceMeasure, Distance<ColorMarker> {

    private double latitude;
    private double longitude;
    private String color;

    public ColorMarker(Marker marker) {
        this.latitude = marker.getLatitude();
        this.longitude = marker.getLongitude();
    }

    @Override
    public double[] getPoint() {
        return new double[] {latitude, longitude};
    }

    @Override
    public double compute(double[] a, double[] b) throws DimensionMismatchException {
        return GeoEarthMathUtils.distance(new Marker(a[0], a[1]), new Marker(b[0], b[1]));
    }

    @Override
    public double d(ColorMarker x, ColorMarker y) {
        return GeoEarthMathUtils.distance(new Marker(x.getLatitude(), x.getLongitude()), new Marker(y.getLatitude(), y.getLongitude()));
    }
}

