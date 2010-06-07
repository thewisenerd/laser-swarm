package com.google.code.laserswarm.math;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point3d;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class PointCloudComparison {

	public static boolean approxEquals(double d1, double d2) {
		return approxEquals(d1, d2, 1E-6);
	}

	public static boolean approxEquals(double d1, double d2, double tolerance) {
		return d1 - tolerance < d2 && d1 + tolerance > d2;
	}

	private List<Point3d>		cloud1;
	private List<Point3d>		cloud2;
	private LinkedList<Double>	diff;

	private Double				max;
	private Double				min;
	private int					mean;
	private Double				median;

	private static final Logger	logger	= Logger.get(PointCloudComparison.class);

	public PointCloudComparison(List<Point3d> cloud1, List<Point3d> cloud2, double rotation,
			boolean fullStore) {

		if (rotation != 0)
			cloud2 = rotate(cloud1);

		LinkedList<Double> diff = Lists.newLinkedList();
		for (Point3d basePoint : cloud1) {
			double baseSize = basePoint.z;

			LinkedList<Point3d> canidates = findClosest(basePoint, cloud2);
			Point3d closest = canidates.getFirst();
			double closestSize = closest.z;
			diff.add(closestSize - baseSize);
		}

		min = diff.getFirst();
		max = diff.getLast();
		mean = 0;
		for (Double double1 : diff) {
			mean += double1;
		}
		mean = mean / diff.size();
		median = diff.get((int) Math.ceil(diff.size() / 2));

		if (fullStore) {
			this.cloud1 = cloud1;
			this.cloud2 = cloud2;
			this.diff = diff;
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

	public LinkedList<Double> getDiff() {
		return diff;
	}

	public Double getMax() {
		return max;
	}

	public int getMean() {
		return mean;
	}

	public Double getMedian() {
		return median;
	}

	public Double getMin() {
		return min;
	}

	private List<Point3d> rotate(List<Point3d> cloud12) {
		// TODO Auto-generated method stub
		// return null;
		throw new UnsupportedOperationException();
	}

}
