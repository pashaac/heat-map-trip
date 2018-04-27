package ru.ifmo.pashaac.heat.map.trip.heatmaptrip.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.distance.DistanceMeasure;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClusterableBoundingBox implements Clusterable, DistanceMeasure {

    private long id;
    private double rating;
    private String color;

    @Override
    public double[] getPoint() {
        return new double[]{rating};
    }

    @Override
    public double compute(double[] a, double[] b) throws DimensionMismatchException {
        return Math.abs(a[0] - b[0]);
    }
}
