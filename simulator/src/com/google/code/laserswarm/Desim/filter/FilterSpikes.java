package com.google.code.laserswarm.Desim.filter;

import java.util.Iterator;
import java.util.LinkedList;

import javax.vecmath.Point3d;

import com.google.common.collect.Lists;

public class FilterSpikes implements Filter {
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
	public LinkedList<Point3d> filter(LinkedList<Point3d> alts) {
		LinkedList<Point3d> results = Lists.newLinkedList();
		Iterator<Point3d> altIt = alts.iterator();
		LinkedList<Point3d> altQueue = Lists.newLinkedList();
		while (altIt.hasNext()) {
			altQueue.add(altIt.next());
			while (altQueue.size() > qLength) {
				altQueue.remove();
			}
			if (altQueue.size() == qLength) {
				Iterator<Point3d> queueIt = altQueue.iterator();
				int count = 0;
				double last = 0.0;
				double current = 0.0;
				boolean stillEqual = true;
				while (queueIt.hasNext()) {
					last = current;
					current = queueIt.next().x;
					if (count == 0) {
						last = current;
						current = queueIt.next().x;
						count++;
					} else if (count == middle) {
						current = queueIt.next().x;
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
					} else {
						queueIt = altQueue.iterator();
						double total = 0;
						double altNo = 0;
						while (queueIt.hasNext()) {
							Point3d localAlt = queueIt.next();
							if (!(altNo == middle)) {
								total += localAlt.x;
							}
							altNo++;
						}
						Point3d middlePoint = altQueue.get(middle);
						results.add(new Point3d(total / (double) altNo, middlePoint.y, middlePoint.z));
					}
				}
			}
		}
		return results;
	}
}
