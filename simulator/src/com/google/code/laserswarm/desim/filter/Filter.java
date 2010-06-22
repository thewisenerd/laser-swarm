package com.google.code.laserswarm.desim.filter;

import java.util.LinkedList;

import javax.vecmath.Point3d;

public interface Filter {
	public LinkedList<Point3d> filter(LinkedList<Point3d> alts);
}
