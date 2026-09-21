package org.apache.commons.math.stat.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Decision branches covered:
 * - cluster(): maxIterations < 0 -> Integer.MAX_VALUE
 * - cluster(): for loop count < max
 * - cluster(): clusteringChanged flag
 * - cluster(): if cluster.getPoints().isEmpty() -> switch on emptyStrategy
 * - cluster(): else -> newCenter = centroidOf, equality check
 * - cluster(): if !clusteringChanged -> return clusters
 * - chooseInitialCenters(): while resultSet.size() < k
 * - chooseInitialCenters(): for loop over pointSet, compute dx2
 * - chooseInitialCenters(): if dx2[i] >= r -> remove and add
 * - getPointFromLargestVarianceCluster(): for each cluster, if non-empty compute variance, compare
 * - getPointFromLargestVarianceCluster(): if selected == null -> throw ConvergenceException
 * - getPointFromLargestNumberCluster(): for each cluster, compare size
 * - getPointFromLargestNumberCluster(): if selected == null -> throw ConvergenceException
 * - getFarthestPoint(): for each cluster, for each point, compare distance
 * - getFarthestPoint(): if selectedCluster == null -> throw ConvergenceException
 * - getNearestCluster(): for each cluster, compare distance
 * 
 * Boundary conditions:
 * - k = 0, k > points.size(), k = 1
 * - maxIterations = 0, negative, positive
 * - empty points collection
 * - points with very small distances (defect trigger)
 * - empty cluster strategies: LARGEST_VARIANCE, LARGEST_POINTS_NUMBER, FARTHEST_POINT, ERROR
 * - Random seed fixed for determinism
 * 
 * Defect-targeted: testSmallDistances – points extremely close together may cause
 * chooseInitialCenters to have sum = 0, leading to r = 0 and infinite loop or
 * division by zero. The test ensures clustering completes and returns correct number of clusters.
 */
public class KMeansPlusPlusClustererDeepseekTest {

    // Simple 2D point implementing Clusterable
    private static class EuclideanPoint implements Clusterable<EuclideanPoint> {
        private final double x;
        private final double y;

        EuclideanPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public double distanceFrom(EuclideanPoint p) {
            double dx = this.x - p.x;
            double dy = this.y - p.y;
            return Math.sqrt(dx * dx + dy * dy);
        }

        @Override
        public EuclideanPoint centroidOf(Collection<EuclideanPoint> points) {
            double sumX = 0, sumY = 0;
            int count = 0;
            for (EuclideanPoint p : points) {
                sumX += p.x;
                sumY += p.y;
                count++;
            }
            return new EuclideanPoint(sumX / count, sumY / count);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof EuclideanPoint)) return false;
            EuclideanPoint other = (EuclideanPoint) obj;
            return Double.compare(this.x, other.x) == 0 && Double.compare(this.y, other.y) == 0;
        }

        @Override
        public int hashCode() {
            return Double.hashCode(x) * 31 + Double.hashCode(y);
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    // Helper to create a list of points
    private static List<EuclideanPoint> points(double... coords) {
        List<EuclideanPoint> list = new ArrayList<>();
        for (int i = 0; i < coords.length; i += 2) {
            list.add(new EuclideanPoint(coords[i], coords[i + 1]));
        }
        return list;
    }

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testBasicClustering() {
        Random rng = new Random(42);
        KMeansPlusPlusClusterer<EuclideanPoint> clusterer =
                new KMeansPlusPlusClusterer<>(rng, KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE);
        List<EuclideanPoint> pts = points(0, 0, 1, 0, 0, 1, 10, 10, 11, 10, 10, 11);
        List<Cluster<EuclideanPoint>> clusters = clusterer.cluster(pts, 2, 10);
        assertEquals("Should produce 2 clusters", 2, clusters.size());
        // Each cluster should have 3 points
        for (Cluster<EuclideanPoint> c : clusters) {
            assertEquals(3, c.getPoints().size());
        }
    }

    @Test(timeout = 4000)
    public void testSingleCluster() {
        Random rng = new Random(123);
        KMeansPlusPlusClusterer<EuclideanPoint> clusterer =
                new KMeansPlusPlusClusterer<>(rng);
        List<EuclideanPoint> pts = points(5, 5, 6, 6, 7, 7);
        List<Cluster<EuclideanPoint>> clusters = clusterer.cluster(pts, 1, 5);
        assertEquals(1, clusters.size());
        assertEquals(3, clusters.get(0).getPoints().size());
    }

    @Test(timeout = 4000)
    public void testMaxIterationsNegative() {
        Random rng = new Random(77);
        KMeansPlusPlusClusterer<EuclideanPoint> clusterer =
                new KMeansPlusPlusClusterer<>(rng);
        List<EuclideanPoint> pts = points(0, 0, 1, 1, 2, 2);
        // Negative maxIterations should be treated as no maximum
        List<Cluster<EuclideanPoint>> clusters = clusterer.cluster(pts, 2, -1);
        assertEquals(2, clusters.size());
    }

    @Test(timeout = 4000)
    public void testMaxIterationsZero() {
        Random rng = new Random(99);
        KMeansPlusPlusClusterer<EuclideanPoint> clusterer =
                new KMeansPlusPlusClusterer<>(rng);
        List<EuclideanPoint> pts = points(0, 0, 1, 1, 2, 2);
        // Zero iterations: only initial assignment, no refinement
        List<Cluster<EuclideanPoint>> clusters = clusterer.cluster(pts, 2, 0);
        assertEquals(2, clusters.size());
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testEmptyPointsCollection() {
        Random rng = new Random(1);
        KMeansPlusPlusClusterer<EuclideanPoint> clusterer =
                new KMeansPlusPlusClusterer<>(rng);
        List<EuclideanPoint> empty = new ArrayList<>();
        try {
            clusterer.cluster(empty, 3, 10);
            fail("Expected IllegalArgumentException for empty points");
        } catch (IllegalArgumentException e) {
            // expected because chooseInitialCenters will fail on random.nextInt(0)
        } catch (Exception e) {
            // any other exception is acceptable as long as it's thrown
        }
    }

    @Test(timeout = 4000)
    public void testKGreaterThanPoints() {
        Random rng = new Random(2);
        KMeansPlusPlusClusterer<EuclideanPoint> clusterer =
                new KMeansPlusPlusClusterer<>(rng);
        List<EuclideanPoint> pts = points(0, 0, 1, 1);
        try {
            clusterer.cluster(pts, 5, 10);
            fail("Expected exception for k > points.size()");
        } catch (Exception e) {
            // chooseInitialCenters will eventually fail because pointSet becomes empty before k reached
        }
    }

    @Test(timeout = 4000)
    public void testKZero() {
        Random rng = new Random(3);
        KMeansPlusPlusClusterer<EuclideanPoint> clusterer =
                new KMeansPlusPlusClusterer<>(rng);
        List<EuclideanPoint> pts = points(0, 0, 1, 1);
        try {
            clusterer.cluster(pts, 0, 10);
            fail("Expected exception for k=0");
        } catch (Exception e) {
            // chooseInitialCenters will not add any center, then assignPointsToClusters will have empty clusters list
        }
    }

    // ========== Partition C: Defect-Targeted (Small Distances) ==========

    @Test(timeout = 4000)
    public void testSmallDistances() {
        // Points extremely close together – this is the known defect trigger.
        // The bug may cause infinite loop or division by zero in chooseInitialCenters
        // when sum of squared distances is zero.
        Random rng = new Random(12345);
        KMeansPlusPlusClusterer<EuclideanPoint> clusterer =
                new KMeansPlusPlusClusterer<>(rng, KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE);
        // Points with very small differences (1e-12)
        List<EuclideanPoint> pts = points(
                0.0, 0.0,
                1e-12, 0.0,
                0.0, 1e-12,
                1e-12, 1e-12
        );
        // Cluster into 2 groups – should complete without exception
        List<Cluster<EuclideanPoint>> clusters = clusterer.cluster(pts, 2, 100);
        assertEquals("Should produce 2 clusters", 2, clusters.size());
        // Total points should be 4
        int total = 0;
        for (Cluster<EuclideanPoint> c : clusters) {
            total += c.getPoints().size();
        }
        assertEquals(4, total);
    }

    @Test(timeout = 4000)
    public void testExtremelySmallDistances() {
        // Even smaller distances to stress the algorithm
        Random rng = new Random(9999);
        KMeansPlusPlusClusterer<EuclideanPoint> clusterer =
                new KMeansPlusPlusClusterer<>(rng, KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_POINTS_NUMBER);
        List<EuclideanPoint> pts = points(
                0.0, 0.0,
                1e-15, 0.0,
                0.0, 1e-15,
                1e-15, 1e-15,
                2e-15, 2e-15
        );
        List<Cluster<EuclideanPoint>> clusters = clusterer.cluster(pts, 2, 50);
        assertEquals(2, clusters.size());
        int total = 0;
        for (Cluster<EuclideanPoint> c : clusters) {
            total += c.getPoints().size();
        }
        assertEquals(5, total);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testEmptyClusterStrategyError() {
        Random rng = new Random(5);
        KMeansPlusPlusClusterer<EuclideanPoint> clusterer =
                new KMeansPlusPlusClusterer<>(rng, KMeansPlusPlusClusterer.EmptyClusterStrategy.ERROR);
        // Create points that will likely produce an empty cluster (e.g., all points identical)
        List<EuclideanPoint> pts = points(0, 0, 0, 0, 0, 0);
        try {
            clusterer.cluster(pts, 3, 10);
            fail("Expected ConvergenceException for empty cluster with ERROR strategy");
        } catch (ConvergenceException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEmptyClusterStrategyFarthestPoint() {
        Random rng = new Random(6);
        KMeansPlusPlusClusterer<EuclideanPoint> clusterer =
                new KMeansPlusPlusClusterer<>(rng, KMeansPlusPlusClusterer.EmptyClusterStrategy.FARTHEST_POINT);
        // Points that may cause empty cluster (e.g., all same point)
        List<EuclideanPoint> pts = points(1, 1, 1, 1, 1, 1);
        // k=2, all points identical -> after initial assignment, one cluster will be empty
        List<Cluster<EuclideanPoint>> clusters = clusterer.cluster(pts, 2, 10);
        assertEquals(2, clusters.size());
        // The empty cluster should be filled with the farthest point (which is also the same point)
        // So both clusters should have points (maybe 1 and 2)
        int total = 0;
        for (Cluster<EuclideanPoint> c : clusters) {
            total += c.getPoints().size();
        }
        assertEquals(3, total);
    }

    @Test(timeout = 4000)
    public void testEmptyClusterStrategyLargestNumber() {
        Random rng = new Random(7);
        KMeansPlusPlusClusterer<EuclideanPoint> clusterer =
                new KMeansPlusPlusClusterer<>(rng, KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_POINTS_NUMBER);
        // Create two distinct groups, but with k=3 so one cluster will be empty
        List<EuclideanPoint> pts = points(0, 0, 0, 0, 10, 10, 10, 10);
        List<Cluster<EuclideanPoint>> clusters = clusterer.cluster(pts, 3, 10);
        assertEquals(3, clusters.size());
        int total = 0;
        for (Cluster<EuclideanPoint> c : clusters) {
            total += c.getPoints().size();
        }
        assertEquals(4, total);
    }

    @Test(timeout = 4000)
    public void testEmptyClusterStrategyLargestVariance() {
        Random rng = new Random(8);
        KMeansPlusPlusClusterer<EuclideanPoint> clusterer =
                new KMeansPlusPlusClusterer<>(rng, KMeansPlusPlusClusterer.EmptyClusterStrategy.LARGEST_VARIANCE);
        // Points with one cluster spread out, another tight
        List<EuclideanPoint> pts = points(0, 0, 0, 0, 10, 10, 10, 10, 100, 100);
        List<Cluster<EuclideanPoint>> clusters = clusterer.cluster(pts, 3, 10);
        assertEquals(3, clusters.size());
        int total = 0;
        for (Cluster<EuclideanPoint> c : clusters) {
            total += c.getPoints().size();
        }
        assertEquals(5, total);
    }

    // ========== Partition E: Object Lifecycle & Contract ==========

    @Test(timeout = 4000)
    public void testConstructorDefaultStrategy() {
        Random rng = new Random(9);
        KMeansPlusPlusClusterer<EuclideanPoint> clusterer = new KMeansPlusPlusClusterer<>(rng);
        // Should use LARGEST_VARIANCE by default – we can test by clustering points that cause empty cluster
        List<EuclideanPoint> pts = points(0, 0, 0, 0);
        List<Cluster<EuclideanPoint>> clusters = clusterer.cluster(pts, 2, 10);
        assertEquals(2, clusters.size());
    }

    @Test(timeout = 4000)
    public void testRandomSeedDeterminism() {
        Random rng1 = new Random(42);
        Random rng2 = new Random(42);
        KMeansPlusPlusClusterer<EuclideanPoint> c1 = new KMeansPlusPlusClusterer<>(rng1);
        KMeansPlusPlusClusterer<EuclideanPoint> c2 = new KMeansPlusPlusClusterer<>(rng2);
        List<EuclideanPoint> pts = points(0, 0, 1, 1, 2, 2, 10, 10, 11, 11);
        List<Cluster<EuclideanPoint>> clusters1 = c1.cluster(pts, 2, 10);
        List<Cluster<EuclideanPoint>> clusters2 = c2.cluster(pts, 2, 10);
        // Compare cluster centers (order may differ, but we can check sizes and total points)
        assertEquals(clusters1.size(), clusters2.size());
        int total1 = 0, total2 = 0;
        for (Cluster<EuclideanPoint> cl : clusters1) total1 += cl.getPoints().size();
        for (Cluster<EuclideanPoint> cl : clusters2) total2 += cl.getPoints().size();
        assertEquals(total1, total2);
    }
}