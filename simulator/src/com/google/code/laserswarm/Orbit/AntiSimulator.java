/**
 * 
 */
package com.google.code.laserswarm.Orbit;

import jat.constants.IERS_1996;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * @author Administrator
 * 
 */
public class AntiSimulator {

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

		Point3d em = new Point3d();
		Point3d rem = new Point3d();
		double xdist = 7000000;
		double r = IERS_1996.R_Earth;
		double km = 1 / IERS_1996.c * 1000;
		double ns = 1E-9;
		double t = 1200 * km;
		em.set(xdist, 0, 0);
		rem.set(xdist - 410, 410, 0);
		double alt = calcalt(em, rem, t);
		System.out.print((xdist - r) / 1000 + "\n" + alt / 1000);

	}

}
