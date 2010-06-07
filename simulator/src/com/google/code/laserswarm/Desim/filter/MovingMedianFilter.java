package com.google.code.laserswarm.Desim.filter;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.vecmath.Point3d;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class MovingMedianFilter extends MovingFilter {

	public MovingMedianFilter(int filterSize) {
		super(filterSize);
	}

	@Override
	protected Point3d filterPoint(ImmutableList<Point3d> previus) {
		List<Point3d> points = Lists.newArrayList(previus);
		Collections.sort(points, new Comparator<Point3d>() {
			@Override
			public int compare(Point3d o1, Point3d o2) {
				Point3d o = new Point3d(0, 0, 0);
				double d1 = o1.distanceSquared(o);
				double d2 = o2.distanceSquared(o);
				return Double.compare(d1, d2);
			}
		});
		return points.get((int) Math.ceil(points.size() / 2));
	}
}
