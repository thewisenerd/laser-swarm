package com.google.code.laserswarm.Desim.filter;

import java.util.LinkedList;
import java.util.ListIterator;

import javax.vecmath.Point3d;

import com.google.code.laserswarm.Desim.elevation.slope.ElevationSlope;
import com.google.code.laserswarm.Desim.filter.elevationslope.ElevationSlopeFilter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public abstract class MovingFilter implements ElevationSlopeFilter {

	private int	filterSize;

	public MovingFilter(int filterSize) {
		this.filterSize = filterSize;
	}

	// public LinkedList<Point3d> filter(LinkedList<Point3d> alts) {
	@Override
	public ElevationSlope filter(ElevationSlope elSlopeData) {
		/* Aliases */
		LinkedList<Point3d> alts = elSlopeData.getAltitudes();
		LinkedList<Double> slopes = elSlopeData.getSlopes();

		LinkedList<Point3d> previus = Lists.newLinkedList();
		LinkedList<Point3d> next = Lists.newLinkedList();

		LinkedList<Point3d> newVals = Lists.newLinkedList();

		ListIterator<Point3d> it = alts.listIterator();
		for (int i = 0; i <= filterSize; i++)
			next.add(it.next());
		next.removeFirst();

		for (int i = 0; i < alts.size(); i++) {
			if (it.hasNext())
				previus.add(it.next());
			if (previus.size() > filterSize * 2 + 1)
				previus.removeFirst();
			newVals.add(filterPoint(ImmutableList.copyOf(previus)));
		}

		return new ElevationSlope(newVals, slopes);
	}

	protected abstract Point3d filterPoint(ImmutableList<Point3d> immutableList);
}