package com.google.code.laserswarm.Desim.filter;

import java.util.LinkedList;
import java.util.ListIterator;

import javax.vecmath.Point3d;

import com.google.common.collect.ImmutableList;
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

		ListIterator<Point3d> it = alts.listIterator();
		for (int i = 0; i <= filterSize; i++)
			previus.add(it.next());

		for (int i = 0; i < alts.size(); i++) {
			if (it.hasNext())
				previus.add(it.next());
			if (previus.size() > filterSize * 2 + 1)
				previus.removeFirst();
			newVals.add(filterPoint(ImmutableList.copyOf(previus)));
		}
		return newVals;
	}

	protected abstract Point3d filterPoint(ImmutableList<Point3d> immutableList);
}