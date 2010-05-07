/**
 * 
 */
package com.google.code.laserswarm.Orbit;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import jat.constants.IERS_1996;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.apache.commons.math.stat.*;

import com.google.code.laserswarm.conf.Satellite;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author Administrator
 * 
 */
public class AntiSimulator {

	public void calcposition() {

	}

	public static double extractpath(Map<Satellite, Vector<Double>> altData) {
		// TODO optimize the search algorithm
		// preallocate capacity of vector
		// use more sets instead of vectors
		// program to return Satellite/time relation

		// pathlength should be equal to num of satellites,
		// number of pathes = maximum number of elements in Vecotr<Double> array
		int pathNum = 0, pathLength = 0;
		Satellite maxSat = new Satellite();
		Vector<Double> initVec;// = new Vector<Double>();
		Vector<Vector<Double>> vertVec = new Vector<Vector<Double>>();// verticle vectors vector
		// containing the path

		for (Satellite a : altData.keySet()) { // find max Vector length
			pathLength++;
			if (pathNum < altData.get(a).size()) {
				maxSat = a; // remember the satellite;
				pathNum = altData.get(a).size();
			}
		}

		TreeSet<Double> distSet = Sets.newTreeSet();
		initVec = altData.get(maxSat);
		// Double[] arr = (Double[]) initVec.toArray();

		for (int i = 0; i < pathNum; i++) {
			Double tmp = initVec.get(i);
			vertVec.get(i).add(tmp);
			for (Satellite eh : altData.keySet()) { // iterate over all the satellites
				if (eh == maxSat)
					continue; // ignore the root element

				distSet.addAll(altData.get(eh)); // set of distances to iterate for a specific satellite
				double ceil = distSet.ceiling(tmp);
				double floor = distSet.floor(tmp);
				if ((ceil - tmp) > (tmp - floor)) // find closest value in the array
				{
					tmp = floor;
				} else {
					tmp = ceil;
				}

				vertVec.get(i).add(tmp); // add the value;
			}
		}

		double var = 0; // variance
		double[] resul = null; // result array
		Vector<Double> selPath = new Vector<Double>(); // selected path
		for (int i = 0; i < pathNum; i++) {
			Double[] ar1 = (Double[]) vertVec.get(i).toArray();
			double[] ar2 = new double[ar1.length];

			for (int j = 0; j < ar1.length; j++) {
				ar2[j] = (double) ar1[j];
			}
			double hm = StatUtils.variance(ar2);
			if (i == 0) {
				var = hm;
				selPath = vertVec.get(i);
			}
			// Select one with the minimal variance
			if (var > hm) { // old bigger than new then
				resul = ar2;
				var = hm;
				selPath = vertVec.get(i);
			}

		}
		return StatUtils.mean(resul);

	}

	public static Map<Double, Integer> findspikes(Map<Double, Integer> data) {
		// TODO Auto-generated method stub
		// Integer sum = new Integer(0);
		int size = data.size();
		int sum = 0;
		int average;
		Map<Double, Integer> hi = Maps.newHashMap();

		for (Map.Entry<Double, Integer> h : data.entrySet()) {
			sum = sum + h.getValue().intValue();
		}
		average = sum / size;
		for (Map.Entry<Double, Integer> h : data.entrySet()) {
			if (h.getValue().intValue() > average)
				;
			{
				hi.put(h.getKey(), h.getValue());
			}
		}
		return hi;
	}

	/**
	 * @param emit
	 *            , rec1 in METERS
	 * @param trav1
	 *            in sec.
	 */

	public static double calcalt(Point3d emit, Point3d rec1, double trav1) {
		// Assumed: Location of the satellite is known to high precission
		// Earth is a perfect sphere
		// Emitter points perp. to the earth center
		// Recievers points to the same point as the emitter

		// create an ellipse
		double f = emit.distance(rec1); // focal distance
		double c = IERS_1996.c; // speed of light
		double dist = trav1 * c;
		double a = dist / 2; // semimajor axis

		System.out.print("a: " + a + "\n");
		double b_2 = Math.pow(dist / 2, 2) - Math.pow(f / 2, 2); // b^2
		System.out.print("b^2: " + b_2 + "\n");
		double eps_2 = Math.sqrt(1 - b_2 / (a * a)); // eccentricity^2
		System.out.print(eps_2 + "\n");
		double eps = Math.sqrt(eps_2); // eccentricity^2
		System.out.print("eps: " + eps + "\n");
		Vector3d em = new Vector3d(emit);
		Vector3d re = new Vector3d(rec1);
		Vector3d dif = new Vector3d();
		dif.sub(em, re);
		double theta = dif.angle(em);
		System.out.print("dist: " + dif.length() + "\n");
		double H = a * (1 - eps_2) / (1 - eps * Math.cos(theta)); // distance to the ground from the
		// emitter
		System.out.print("Gdist: " + H + "\n");

		return em.length() - IERS_1996.R_Earth - H; // altitude above the earth sphere in meters

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 

		/*
		 * Point3d em = new Point3d(); Point3d rem = new Point3d(); double xdist = 7000000; double r =
		 * IERS_1996.R_Earth; double km = 1 / IERS_1996.c * 1000; double ns = 1E-9; double t = 1200 * km;
		 * em.set(xdist, 0, 0); rem.set(xdist - 410, 410, 0); double alt = calcalt(em, rem, t);
		 * System.out.print((xdist - r) / 1000 + "\n" + alt / 1000);
		 */

	}

}
