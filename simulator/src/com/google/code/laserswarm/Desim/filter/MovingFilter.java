package com.google.code.laserswarm.Desim.filter;

import java.util.LinkedList;

import javax.vecmath.Point3d;

import com.google.common.collect.Lists;

public abstract class MovingFilter implements Filter {

	private int	filterSize;

	public MovingFilter(int filterSize) {
		this.filterSize = filterSize;
	}

	@Override
	public LinkedList<Point3d> filter(LinkedList<Point3d> alts) {
		LinkedList<Point3d> previus = Lists.newLinkedList();
		LinkedList<Point3d> newVals = Lists.newLinkedList();

		for (Point3d point : alts) {
			previus.add(point);
			if (previus.size() > filterSize)
				previus.removeFirst();
			newVals.add(filterPoint(previus));
		}
		return newVals;
	}

	protected abstract Point3d filterPoint(LinkedList<Point3d> previus);
}