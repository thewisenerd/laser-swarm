package com.google.code.laserswarm.Desim;

import java.util.Iterator;
import java.util.LinkedList;

import javax.vecmath.Point3d;

import com.google.common.collect.Lists;

public class FilterOutlierRemoval implements Filter {
	private int	sigmaInterval	= 200;
	private int	meanInterval	= 25;

	public FilterOutlierRemoval(int sigmaInt, int meanInt) {
		sigmaInterval = sigmaInt;
		meanInterval = meanInt;
	}

	public FilterOutlierRemoval() {
	}

	private LinkedList<Double> means(LinkedList<Point3d> alts, int interval) {
		LinkedList<Double> result = Lists.newLinkedList();
		double mean = 0;
		Iterator<Point3d> altFirst = alts.iterator();
		int count = 0;
		int untreated = 0;
		for (Point3d pt : alts) {
			count++;
			untreated++;
			mean += pt.x;
			if (count >= interval) {
				while (untreated > 0) {
					double res = mean / interval;
					result.add(res);
					untreated--;
				}
				mean -= altFirst.next().x;
			}
		}
		Double last = result.getLast();
		while (result.size() != alts.size()) {
			result.add(last);
		}
		return result;
	}

	private LinkedList<Double> sigma(LinkedList<Point3d> alts, LinkedList<Double> means, int interval) {
		LinkedList<Double> result = Lists.newLinkedList();
		Iterator<Double> meanIt = means.iterator();
		Iterator<Double> meanFirst = means.iterator();
		double sigma = 0;
		int count = 0;
		int untreated = 0;
		for (Point3d pt : alts) {
			count++;
			untreated++;
			sigma += Math.pow(pt.x - meanIt.next(), 2);
			if (count >= interval) {
				while (untreated > 0) {
					double res = Math.sqrt(sigma / (interval - 1));
					result.add(res);
					untreated--;
				}
				sigma -= Math.pow(pt.x - meanFirst.next(), 2);
			}
		}
		Double last = result.getLast();
		while (result.size() != alts.size()) {
			result.add(last);
		}
		return result;
	}

	@Override
	public LinkedList<Point3d> filter(LinkedList<Point3d> alts) {
		LinkedList<Point3d> result = Lists.newLinkedList();
		LinkedList<Point3d> empties = Lists.newLinkedList();
		LinkedList<Double> sigmas = sigma(alts, means(alts, sigmaInterval), sigmaInterval);
		LinkedList<Double> means = means(alts, meanInterval);
		Iterator<Double> meanIt = means.iterator();
		Iterator<Double> sigmIt = sigmas.iterator();
		int count = 0;
		int size = alts.size();
		for (Point3d pt : alts) {
			count++;
			if (count == 1 || count == size || Math.abs(pt.x - meanIt.next()) < 2 * sigmIt.next()) {
				result.add(new Point3d(pt));
			} else {
				empties.add(new Point3d(Double.MAX_VALUE, pt.y, pt.z));
				result.add(empties.getLast());
			}
		}
		int numEmpty = 0;
		double first = Double.MAX_VALUE;
		double last = Double.MAX_VALUE;
		for (Point3d pt : result) {
			if (pt.x == Double.MAX_VALUE) {
				numEmpty++;
			} else {
				first = last;
				last = pt.x;
				if (first != Double.MAX_VALUE && numEmpty > 0) {
					double step = (last - first) / (numEmpty + 1);
					int i = 0;
					while (numEmpty > 0) {
						i++;
						numEmpty--;
						empties.getFirst().x = first + i * step;
						empties.removeFirst();
					}
				}
			}
		}
		return result;
	}
}
