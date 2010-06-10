package com.google.code.laserswarm.Desim.elevation;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point3d;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.geotools.referencing.operation.projection.PointOutsideEnvelopeException;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.out.plot1D.plotHeightDistribution;
import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class ElevationComparison extends DescriptiveStatistics {

	public ElevationComparison(EarthModel earth, List<Point3d> set1) {
		super();
		Iterator<Point3d> it1 = set1.iterator();

		LinkedList<Point3d> alts = Lists.newLinkedList();

		while (it1.hasNext()) {
			Point3d pSphere = it1.next();
			double h = -1;
			try {
				h = earth.getElevation(pSphere) + Configuration.R0;
			} catch (PointOutsideEnvelopeException e) {
				e.printStackTrace();
			}
			addValue(pSphere.x - h);
			System.out.println(new Point3d(h, pSphere.y, pSphere.z));
			alts.add(new Point3d(h, pSphere.y, pSphere.z));
		}

		plotHeightDistribution plotter = new plotHeightDistribution();
		try {
			plotter.plot(alts, 3, "SomeImage");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ElevationComparison(List<Point3d> set1, List<Point3d> set2) {
		super();
		Iterator<Point3d> it1 = set1.iterator();
		Iterator<Point3d> it2 = set2.iterator();

		Point3d o = new Point3d(0, 0, 0);
		while (it1.hasNext() && it2.hasNext()) {
			Point3d p1 = it1.next();
			Point3d p2 = it2.next();
			addValue(p2.x - p1.x);
		}
	}
}