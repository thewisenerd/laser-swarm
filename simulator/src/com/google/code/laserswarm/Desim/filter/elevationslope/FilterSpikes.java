package com.google.code.laserswarm.Desim.filter.elevationslope;

import java.util.Iterator;
import java.util.LinkedList;

import javax.vecmath.Point3d;

import com.google.code.laserswarm.Desim.elevation.slope.ElevationSlope;
import com.google.common.collect.Lists;

public class FilterSpikes implements ElevationSlopeFilter {
	private int		qLength			= 5;
	private int		middle			= 2;
	private double	equalitySpacing	= 1.0;

	private boolean areEqual(double a, double b) {
		if (Math.abs(a - b) < equalitySpacing) {
			return true;
		} else {
			return false;
		}
	}

	public FilterSpikes(int queueLength, double whenEqual) {
		qLength = queueLength;
		middle = (int) Math.floor(((double) qLength) / 2.0);
		equalitySpacing = whenEqual;
	}

	@Override
	public ElevationSlope filter(ElevationSlope elSlopeData) {
		LinkedList<Point3d> results = Lists.newLinkedList();
		LinkedList<Double> slopes = Lists.newLinkedList();
		LinkedList<Point3d> altData = elSlopeData.getAltitudes();
		LinkedList<Double> slopeData = elSlopeData.getSlopes();
		Iterator<Point3d> altIt = altData.iterator();
		Iterator<Double> slopeIt = slopeData.iterator();
		LinkedList<Point3d> altQueue = Lists.newLinkedList();
		LinkedList<Double> slopeQueue = Lists.newLinkedList();
		while (altIt.hasNext()) {
			altQueue.add(altIt.next());
			slopeQueue.add(slopeIt.next());
			while (altQueue.size() > qLength) {
				altQueue.remove();
				slopeQueue.remove();
			}
			if (altQueue.size() == qLength) {
				Iterator<Point3d> altQIt = altQueue.iterator();
				Iterator<Double> slopeQIt = slopeQueue.iterator();
				int count = 0;
				double last = 0.0;
				double current = 0.0;
				boolean stillEqual = true;
				while (altQIt.hasNext()) {
					last = current;
					current = altQIt.next().x;
					if (count == 0) {
						last = current;
						current = altQIt.next().x;
						count++;
					} else if (count == middle) {
						current = altQIt.next().x;
						count++;
					}
					if (!areEqual(last, current)) {
						stillEqual = false;
					}
					count++;
				}
				if (stillEqual) {
					if (areEqual(altQueue.getFirst().x, altQueue.get(middle).x)) {
						results.add(altQueue.get(middle));
						slopes.add(slopeQueue.get(middle));
					} else {
						altQIt = altQueue.iterator();
						slopeQIt = slopeQueue.iterator();
						double total = 0;
						double totalSlope = 0;
						double altNo = 0;
						while (altQIt.hasNext()) {
							Point3d localAlt = altQIt.next();
							Double localSlope = slopeQIt.next();
							if (!(altNo == middle)) {
								total += localAlt.x;
								totalSlope += localSlope;
							}
							altNo++;
						}
						Point3d middlePoint = altQueue.get(middle);
						results.add(new Point3d(total / (double) altNo, middlePoint.y, middlePoint.z));
						slopes.add(totalSlope / (double) altNo);
					}
				}
			}
		}
		return new ElevationSlope(results, slopes);
	}
}
