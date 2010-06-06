package com.google.code.laserswarm.Desim.filter;

import java.util.LinkedList;

import javax.vecmath.Point3d;

import com.google.common.collect.ImmutableSet;

public class MovingMedianFilter extends MovingFilter {

	public MovingMedianFilter(int filterSize) {
		super(filterSize);
	}

	@Override
	protected Point3d filterPoint(LinkedList<Point3d> previus) {
		ImmutableSet<Point3d> points = ImmutableSet.copyOf(previus);
		Point3d sum = new Point3d();
		for (Point3d point3d : previus) {
			sum.add(point3d);
		}
		sum.scale(previus.size());
		return sum;
	}
}
