package com.google.code.laserswarm.Desim.filter;

import javax.vecmath.Point3d;

import com.google.common.collect.ImmutableList;

public class MovingAverageFilter extends MovingFilter {

	public MovingAverageFilter(int filterSize) {
		super(filterSize);
	}

	@Override
	protected Point3d filterPoint(ImmutableList<Point3d> previus) {
		Point3d sum = new Point3d();
		for (Point3d point3d : previus) {
			sum.add(point3d);
		}
		sum.scale(1. / previus.size());
		return sum;
	}

}
