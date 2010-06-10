package com.google.code.laserswarm.math;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Point4d;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class DistributionComparison extends DescriptiveStatistics {

	private static final Logger	logger	= Logger.get(DistributionComparison.class);

	public static boolean approxEquals(double d1, double d2) {
		return approxEquals(d1, d2, 1E-6);
	}

	public static boolean approxEquals(double d1, double d2, double tolerance) {
		return d1 - tolerance < d2 && d1 + tolerance > d2;
	}

	private List<Point3d>	cloud1;
	private List<Point3d>	cloud2;

	/**
	 * Compare over an entire hemisphere
	 * <p>
	 * Azimuth and alevation range:<br/>
	 * [0, 2&pi;, 0, &pi;/2]
	 * </p>
	 * 
	 * @param distribution
	 * @param distribution2
	 * @param fullStore
	 */
	public DistributionComparison(Distribution distribution, Distribution distribution2,
			boolean fullStore) {
		this(distribution, distribution2, new Point4d(0, Math.PI * 2, 0, Math.PI / 2), fullStore);
	}

	/**
	 * Compare two distributions
	 * 
	 * @param distribution
	 *            First distribution to compare
	 * @param distribution2
	 *            Second distribution
	 * @param range
	 *            [azMin, azMax, elMin, elMax] Ranges of azimuth and elevation.
	 * @param fullStore
	 *            Store all the values of the cloud
	 */
	public DistributionComparison(Distribution distribution, Distribution distribution2, Point4d range,
			boolean fullStore) {

		List<Point3d> cloud1 = distribution.pointCloud(100, range);
		List<Point3d> cloud2 = distribution2.pointCloud(100, range);

		for (Point3d basePoint : cloud1) {
			double baseSize = basePoint.z;

			LinkedList<Point3d> canidates = findClosest(basePoint, cloud2);
			Point3d closest = canidates.getFirst();
			double closestSize = closest.z;
			addValue(closestSize - baseSize);
		}

		if (fullStore) {
			this.cloud1 = cloud1;
			this.cloud2 = cloud2;
		}

	}

	private LinkedList<Point3d> findClosest(Point3d basePoint, List<Point3d> cloud2) {
		final double baseAz = basePoint.x;
		final double baseEl = basePoint.y;

		LinkedList<Point3d> canidates = Lists.newLinkedList(Collections2.filter(cloud2,
				new Predicate<Point3d>() {
					@Override
					public boolean apply(Point3d input) {
						return approxEquals(baseAz, input.x) && approxEquals(baseEl, input.y);
					}
				}));

		if (canidates.size() == 0) {
			logger.wrn("Did not find any canidates for point %s in cloud2", basePoint);
			return canidates;
		}

		Collections.sort(canidates, new Comparator<Point3d>() {
			@Override
			public int compare(Point3d o1, Point3d o2) {
				double dist1 = Math.pow(baseAz - o1.x, 2) + Math.pow(baseEl - o1.y, 2);
				double dist2 = Math.pow(baseAz - o2.x, 2) + Math.pow(baseEl - o2.y, 2);
				return Double.compare(dist1, dist2);
			}
		});

		return canidates;
	}

	public List<Point3d> getCloud1() {
		return cloud1;
	}

	public List<Point3d> getCloud2() {
		return cloud2;
	}

}
