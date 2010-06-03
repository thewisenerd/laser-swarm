package com.google.code.laserswarm.Desim.filter;

import java.util.ArrayList;
import java.util.LinkedList;

import javax.vecmath.Point3d;

import com.google.common.collect.Lists;

public class FilterAverage implements Filter {
	private int	width	= 13;

	public FilterAverage(int w) {
		width = w;
	}

	public FilterAverage() {
	}

	@Override
	public LinkedList<Point3d> filter(LinkedList<Point3d> alts) {
		if (width < 1) {
			width = 1;
		}
		LinkedList<Point3d> result = Lists.newLinkedList();
		ArrayList<Point3d> temp = Lists.newArrayList();
		int count = 0;
		int mid = (int) Math.floor(0.5 * width);
		for (Point3d point : alts) {
			count++;
			temp.add(point);
			while (temp.size() > width) {
				temp.remove(0);
			}
			if (temp.size() == width) {
				double tot = 0;
				for (Point3d pt : temp) {
					tot += pt.x;
				}
				tot = tot / width;
				Point3d sat = alts.get(count - mid);
				result.add(new Point3d(tot, sat.y, sat.z));
			}
		}
		return result;
	}

}
