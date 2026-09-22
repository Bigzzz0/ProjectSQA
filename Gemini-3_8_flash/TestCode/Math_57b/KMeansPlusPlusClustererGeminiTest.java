package org.apache.commons.math.stat.clustering;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.math.stat.clustering.KMeansPlusPlusClusterer
 * Target Defect: Defects4J Known Defect (MATH / KMeansPlusPlusClustererTest::testSmallDistances)
 *
 * Decision / Condition Coverage Targets:
 * 1. chooseInitialCenters:
 *    - Bug target: 'int sum = 0;' truncates d*d when d < 1, causing sum to stay 0 and
 *      dx2[i] >= r to always match index 0, failing proper probabilistic center selection.
 *    - resultSet.size() < k condition (k = 1, k = N, k < N).
 *    - dx2[i] >= r boundary condition during center sampling.
 * 2. cluster:
 *    - maxIterations < 0 -> max = Integer.MAX_VALUE branch.
 *    - maxIterations == 0 -> loop does not execute branch.
 *    - maxIterations > 0 -> loop executes up to max branch.
 *    - cluster.getPoints().isEmpty() -> true (EmptyClusterStrategy dispatch) vs false.
 *    - Switch on emptyStrategy:
 *      * LARGEST_VARIANCE -> calls getPointFromLargestVarianceCluster.
 *      * LARGEST_POINTS_NUMBER -> calls getPointFromLargestNumberCluster.
 *      * FARTHEST_POINT -> calls getFarthestPoint.
 *      * default (ERROR) -> throws ConvergenceException.
 *    - !newCenter.equals(cluster.getCenter()) -> true (clusteringChanged = true) vs false.
 *    - !clusteringChanged -> early exit / return clusters.
 * 3. getPointFromLargestVarianceCluster:
 *    - !cluster.getPoints().isEmpty() evaluation.
 *    - variance > maxVariance comparison.
 *    - selected == null defensive branch -> throws ConvergenceException.
 * 4. getPointFromLargestNumberCluster:
 *    - number > maxNumber comparison.
 *    - selected == null defensive branch -> throws ConvergenceException.
 * 5. getFarthestPoint:
 *    - distance > maxDistance comparison.
 *    - selectedCluster == null defensive branch -> throws ConvergenceException.
 * 6. getNearestCluster:
 *    - distance < minDistance tracking closest cluster.
 * -----------------------------------------------------------------------------------------
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.math.exception.ConvergenceException;
import org.apache.commons.math.stat.clustering.KMeansPlusPlusClusterer.EmptyClusterStrategy;
import org.junit.Test;

import static org.junit.Assert.*;

public class KMeansPlusPlusClustererGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testClusterSingleK() {
        List<DoublePoint> points = Arrays.asList(
            new DoublePoint(new double[] { 1.0, 1.0 }),
            new DoublePoint(new double[] { 2.0, 2.0 }),
            new DoublePoint(new double[] { 3.0, 3.0 })
        );

        KMeansPlusPlusClusterer<DoublePoint> clusterer =
            new KMeansPlusPlusClusterer<DoublePoint>(new Random(42L));
        List<Cluster<DoublePoint>> clusters = clusterer.cluster(points, 1, 10);

        assertEquals(1, clusters.size());
        assertEquals(3, clusters.get(0).getPoints().size());
        assertArrayEquals(new double[] { 2.0, 2.0 }, clusters.get(0).getCenter().getPoint(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testClusterNormalConvergence() {
        List<DoublePoint> points = Arrays.asList(
            new DoublePoint(new double[] { 10.0, 10.0 }),
            new DoublePoint(new double[] { 10.2, 10.1 }),
            new DoublePoint(new double[] { 100.0, 100.0 }),
            new DoublePoint(new double[] { 100.1, 99.9 })
        );

        KMeansPlusPlusClusterer<DoublePoint> clusterer =
            new KMeansPlusPlusClusterer<DoublePoint>(new Random(42L));
        List<Cluster<DoublePoint>> clusters = clusterer.cluster(points, 2, 50);

        assertEquals(2, clusters.size());
        int countA = clusters.get(0).getPoints().size();
        int countB = clusters.get(1).getPoints().size();
        assertTrue((countA == 2 && countB == 2));
    }

    @Test(timeout = 4000)
    public void testClusterEarlyTerminationWhenUnchanged() {
        // Points that already equal cluster centers
        List<DoublePoint> points = Arrays.asList(
            new DoublePoint(new double[] { 0.0 }),
            new DoublePoint(new double[] { 100.0 })
        );

        KMeansPlusPlusClusterer<DoublePoint> clusterer =
            new KMeansPlusPlusClusterer<DoublePoint>(new Random(42L));
        // maxIterations is large, but should return early on iteration 1
        List<Cluster<DoublePoint>> clusters = clusterer.cluster(points, 2, 1000);

        assertEquals(2, clusters.size());
    }

    @Test(timeout = 4000)
    public void testClusterZeroMaxIterations() {
        List<DoublePoint> points = Arrays.asList(
            new DoublePoint(new double[] { 1.0, 2.0 }),
            new DoublePoint(new double[] { 10.0, 20.0 })
        );

        KMeansPlusPlusClusterer<DoublePoint> clusterer =
            new KMeansPlusPlusClusterer<DoublePoint>(new Random(42L));
        // maxIterations = 0 ensures the loop does not run at all
        List<Cluster<DoublePoint>> clusters = clusterer.cluster(points, 2, 0);

        assertEquals(2, clusters.size());
    }

    @Test(timeout = 4000)
    public void testClusterNegativeMaxIterations() {
        List<DoublePoint> points = Arrays.asList(
            new DoublePoint(new double[] { 1.0 }),
            new DoublePoint(new double[] { 100.0 })
        );

        KMeansPlusPlusClusterer<DoublePoint> clusterer =
            new KMeansPlusPlusClusterer<DoublePoint>(new Random(42L));
        // Negative maxIterations tests the (maxIterations < 0) ? Integer.MAX_VALUE branch
        List<Cluster<DoublePoint>> clusters = clusterer.cluster(points, 2, -1);

        assertEquals(2, clusters.size());
    }

    @Test(timeout = 4000)
    public void testConstructors() {
        Random random = new Random(12345L);
        KMeansPlusPlusClusterer<DoublePoint> clustererDefault =
            new KMeansPlusPlusClusterer<DoublePoint>(random);
        assertNotNull(clustererDefault);

        KMeansPlusPlusClusterer<DoublePoint> clustererStrategy =
            new KMeansPlusPlusClusterer<DoublePoint>(random, EmptyClusterStrategy.FARTHEST_POINT);
        assertNotNull(clustererStrategy);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Empty Cluster Strategies
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyClusterStrategyErrorThrowsException() {
        // When identical points are present, duplicate centers can result in an empty cluster
        List<DoublePoint> points = Arrays.asList(
            new DoublePoint(new double[] { 0.0 }),
            new DoublePoint(new double[] { 0.0 }),
            new DoublePoint(new double[] { 0.0 })
        );

        KMeansPlusPlusClusterer<DoublePoint> clusterer =
            new KMeansPlusPlusClusterer<DoublePoint>(new Random(1L), EmptyClusterStrategy.ERROR);

        try {
            clusterer.cluster(points, 2, 10);
            fail("Expected ConvergenceException when empty cluster is encountered with ERROR strategy");
        } catch (ConvergenceException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testEmptyClusterStrategyLargestVariance() {
        List<DoublePoint> points = Arrays.asList(
            new DoublePoint(new double[] { 0.0 }),
            new DoublePoint(new double[] { 0.0 }),
            new DoublePoint(new double[] { 0.0 }),
            new DoublePoint(new double[] { 100.0 })
        );

        KMeansPlusPlusClusterer<DoublePoint> clusterer =
            new KMeansPlusPlusClusterer<DoublePoint>(new Random(1L), EmptyClusterStrategy.LARGEST_VARIANCE);

        List<Cluster<DoublePoint>> clusters = clusterer.cluster(points, 2, 10);
        assertEquals(2, clusters.size());
        assertFalse(clusters.get(0).getPoints().isEmpty());
        assertFalse(clusters.get(1).getPoints().isEmpty());
    }

    @Test(timeout = 4000)
    public void testEmptyClusterStrategyLargestPointsNumber() {
        List<DoublePoint> points = Arrays.asList(
            new DoublePoint(new double[] { 0.0 }),
            new DoublePoint(new double[] { 0.0 }),
            new DoublePoint(new double[] { 0.0 }),
            new DoublePoint(new double[] { 50.0 })
        );

        KMeansPlusPlusClusterer<DoublePoint> clusterer =
            new KMeansPlusPlusClusterer<DoublePoint>(new Random(1L), EmptyClusterStrategy.LARGEST_POINTS_NUMBER);

        List<Cluster<DoublePoint>> clusters = clusterer.cluster(points, 2, 10);
        assertEquals(2, clusters.size());
        assertFalse(clusters.get(0).getPoints().isEmpty());
        assertFalse(clusters.get(1).getPoints().isEmpty());
    }

    @Test(timeout = 4000)
    public void testEmptyClusterStrategyFarthestPoint() {
        List<DoublePoint> points = Arrays.asList(
            new DoublePoint(new double[] { 0.0 }),
            new DoublePoint(new double[] { 0.0 }),
            new DoublePoint(new double[] { 0.0 }),
            new DoublePoint(new double[] { 200.0 })
        );

        KMeansPlusPlusClusterer<DoublePoint> clusterer =
            new KMeansPlusPlusClusterer<DoublePoint>(new Random(1L), EmptyClusterStrategy.FARTHEST_POINT);

        List<Cluster<DoublePoint>> clusters = clusterer.cluster(points, 2, 10);
        assertEquals(2, clusters.size());
        assertFalse(clusters.get(0).getPoints().isEmpty());
        assertFalse(clusters.get(1).getPoints().isEmpty());
    }

    @Test(timeout = 4000)
    public void testEmptyClusterDefensiveNullSelectedLargestVariance() {
        List<DynamicPoint> points = Arrays.asList(
            new DynamicPoint(1, 0.0, true),
            new DynamicPoint(2, 0.0, true)
        );

        KMeansPlusPlusClusterer<DynamicPoint> clusterer =
            new KMeansPlusPlusClusterer<DynamicPoint>(new Random(1L), EmptyClusterStrategy.LARGEST_VARIANCE);

        try {
            clusterer.cluster(points, 2, 10);
            fail("Expected ConvergenceException when all clusters are empty");
        } catch (ConvergenceException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testEmptyClusterDefensiveNullSelectedLargestNumber() {
        List<DynamicPoint> points = Arrays.asList(
            new DynamicPoint(1, 0.0, true),
            new DynamicPoint(2, 0.0, true)
        );

        KMeansPlusPlusClusterer<DynamicPoint> clusterer =
            new KMeansPlusPlusClusterer<DynamicPoint>(new Random(1L), EmptyClusterStrategy.LARGEST_POINTS_NUMBER);

        try {
            clusterer.cluster(points, 2, 10);
            fail("Expected ConvergenceException when all clusters are empty");
        } catch (ConvergenceException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testEmptyClusterDefensiveNullSelectedFarthestPoint() {
        List<DynamicPoint> points = Arrays.asList(
            new DynamicPoint(1, 0.0, true),
            new DynamicPoint(2, 0.0, true)
        );

        KMeansPlusPlusClusterer<DynamicPoint> clusterer =
            new KMeansPlusPlusClusterer<DynamicPoint>(new Random(1L), EmptyClusterStrategy.FARTHEST_POINT);

        try {
            clusterer.cluster(points, 2, 10);
            fail("Expected ConvergenceException when all clusters are empty");
        } catch (ConvergenceException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH Defects4J ground truth)
    // =========================================================================

    /**
     * Targets the integer truncation defect in {@code chooseInitialCenters}:
     * {@code int sum = 0;} causes {@code sum += d * d;} to evaluate to 0 whenever
     * distance squared is less than 1.0. Consequently, {@code r = random.nextDouble() * sum}
     * remains 0.0, which forces {@code dx2[0] >= r} to immediately match index 0
     * instead of sampling centers proportionally to D(x)^2.
     *
     * This test specifies the correct expected behavior: 3 distinct tight clusters
     * with distances < 1 must be separated into 3 clusters of exactly 2 points each.
     */
    @Test(timeout = 4000)
    public void testSmallDistances() {
        List<DoublePoint> points = new ArrayList<DoublePoint>();
        // Cluster 1 around (0.0, 0.0)
        points.add(new DoublePoint(new double[] { 0.0, 0.0 }));
        points.add(new DoublePoint(new double[] { 0.0001, 0.0001 }));
        // Cluster 2 around (0.1, 0.1)
        points.add(new DoublePoint(new double[] { 0.1, 0.1 }));
        points.add(new DoublePoint(new double[] { 0.1001, 0.1001 }));
        // Cluster 3 around (0.2, 0.2)
        points.add(new DoublePoint(new double[] { 0.2, 0.2 }));
        points.add(new DoublePoint(new double[] { 0.2001, 0.2001 }));

        KMeansPlusPlusClusterer<DoublePoint> clusterer =
            new KMeansPlusPlusClusterer<DoublePoint>(new Random(0L));
        List<Cluster<DoublePoint>> clusters = clusterer.cluster(points, 3, 100);

        assertEquals(3, clusters.size());
        for (Cluster<DoublePoint> cluster : clusters) {
            assertEquals("Each cluster must have exactly 2 points", 2, cluster.getPoints().size());
        }
    }

    @Test(timeout = 4000)
    public void testChooseInitialCentersExactBoundaries() {
        // Point count equals k
        List<DoublePoint> points = Arrays.asList(
            new DoublePoint(new double[] { 10.0 }),
            new DoublePoint(new double[] { 20.0 }),
            new DoublePoint(new double[] { 30.0 })
        );

        KMeansPlusPlusClusterer<DoublePoint> clusterer =
            new KMeansPlusPlusClusterer<DoublePoint>(new Random(42L));
        List<Cluster<DoublePoint>> clusters = clusterer.cluster(points, 3, 10);

        assertEquals(3, clusters.size());
        for (Cluster<DoublePoint> cluster : clusters) {
            assertEquals(1, cluster.getPoints().size());
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyPointsListThrowsException() {
        List<DoublePoint> emptyPoints = Collections.emptyList();
        KMeansPlusPlusClusterer<DoublePoint> clusterer =
            new KMeansPlusPlusClusterer<DoublePoint>(new Random(42L));

        try {
            clusterer.cluster(emptyPoints, 1, 10);
            fail("Expected exception when points list is empty");
        } catch (IllegalArgumentException | ArithmeticException | IndexOutOfBoundsException expected) {
            // Expected failure due to empty point set
        }
    }

    @Test(timeout = 4000)
    public void testKGreaterThanPointsSizeThrowsException() {
        List<DoublePoint> points = Collections.singletonList(new DoublePoint(new double[] { 1.0 }));
        KMeansPlusPlusClusterer<DoublePoint> clusterer =
            new KMeansPlusPlusClusterer<DoublePoint>(new Random(42L));

        try {
            clusterer.cluster(points, 2, 10);
            fail("Expected exception when k > points.size()");
        } catch (IndexOutOfBoundsException | IllegalArgumentException expected) {
            // Expected failure when k exceeds points count
        }
    }

    @Test(timeout = 4000)
    public void testEmptyClusterStrategyEnumCoverage() {
        EmptyClusterStrategy[] strategies = EmptyClusterStrategy.values();
        assertTrue(strategies.length >= 4);

        assertEquals(EmptyClusterStrategy.LARGEST_VARIANCE,
            EmptyClusterStrategy.valueOf("LARGEST_VARIANCE"));
        assertEquals(EmptyClusterStrategy.LARGEST_POINTS_NUMBER,
            EmptyClusterStrategy.valueOf("LARGEST_POINTS_NUMBER"));
        assertEquals(EmptyClusterStrategy.FARTHEST_POINT,
            EmptyClusterStrategy.valueOf("FARTHEST_POINT"));
        assertEquals(EmptyClusterStrategy.ERROR,
            EmptyClusterStrategy.valueOf("ERROR"));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & EuclideanIntegerPoint Integration
    // =========================================================================

    @Test(timeout = 4000)
    public void testEuclideanIntegerPointIntegration() {
        List<EuclideanIntegerPoint> points = Arrays.asList(
            new EuclideanIntegerPoint(new int[] { 0, 0 }),
            new EuclideanIntegerPoint(new int[] { 1, 0 }),
            new EuclideanIntegerPoint(new int[] { 0, 1 }),
            new EuclideanIntegerPoint(new int[] { 100, 100 }),
            new EuclideanIntegerPoint(new int[] { 101, 100 }),
            new EuclideanIntegerPoint(new int[] { 100, 101 })
        );

        KMeansPlusPlusClusterer<EuclideanIntegerPoint> clusterer =
            new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(new Random(42L));
        List<Cluster<EuclideanIntegerPoint>> clusters = clusterer.cluster(points, 2, 50);

        assertEquals(2, clusters.size());
        int totalAssigned = clusters.get(0).getPoints().size() + clusters.get(1).getPoints().size();
        assertEquals(6, totalAssigned);
    }

    // =========================================================================
    // Test Double Implementations (Deterministic Clusterables)
    // =========================================================================

    /**
     * Robust double-precision Clusterable supporting arbitrary vector dimensions.
     */
    private static class DoublePoint implements Clusterable<DoublePoint>, Serializable {
        private static final long serialVersionUID = 1L;
        private final double[] point;

        public DoublePoint(final double[] point) {
            this.point = point;
        }

        public double[] getPoint() {
            return point;
        }

        @Override
        public double distanceFrom(final DoublePoint p) {
            double sum = 0.0;
            for (int i = 0; i < point.length; i++) {
                final double dp = point[i] - p.point[i];
                sum += dp * dp;
            }
            return Math.sqrt(sum);
        }

        @Override
        public DoublePoint centroidOf(final Collection<DoublePoint> points) {
            final double[] centroid = new double[point.length];
            for (final DoublePoint p : points) {
                for (int i = 0; i < centroid.length; i++) {
                    centroid[i] += p.point[i];
                }
            }
            for (int i = 0; i < centroid.length; i++) {
                centroid[i] /= points.size();
            }
            return new DoublePoint(centroid);
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof DoublePoint)) {
                return false;
            }
            final DoublePoint otherPoint = (DoublePoint) other;
            if (point.length != otherPoint.point.length) {
                return false;
            }
            for (int i = 0; i < point.length; i++) {
                if (Double.compare(point[i], otherPoint.point[i]) != 0) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(point);
        }

        @Override
        public String toString() {
            return Arrays.toString(point);
        }
    }

    /**
     * Controllable Clusterable point to trigger defensive empty-cluster branches.
     */
    private static class DynamicPoint implements Clusterable<DynamicPoint>, Serializable {
        private static final long serialVersionUID = 1L;
        private final int id;
        private final double coord;
        private final boolean clearPointsOnCentroid;

        public DynamicPoint(final int id, final double coord, final boolean clearPointsOnCentroid) {
            this.id = id;
            this.coord = coord;
            this.clearPointsOnCentroid = clearPointsOnCentroid;
        }

        @Override
        public double distanceFrom(final DynamicPoint p) {
            return Math.abs(this.coord - p.coord);
        }

        @Override
        public DynamicPoint centroidOf(final Collection<DynamicPoint> points) {
            if (clearPointsOnCentroid) {
                // Empties the cluster's internal list to simulate an all-clusters-empty state
                points.clear();
            }
            double sum = 0.0;
            for (final DynamicPoint p : points) {
                sum += p.coord;
            }
            final double avg = points.isEmpty() ? coord : sum / points.size();
            return new DynamicPoint(id, avg, clearPointsOnCentroid);
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof DynamicPoint)) {
                return false;
            }
            final DynamicPoint o = (DynamicPoint) other;
            return this.id == o.id && Double.compare(this.coord, o.coord) == 0;
        }

        @Override
        public int hashCode() {
            return 31 * id + Double.valueOf(coord).hashCode();
        }
    }
}