/**
 * 
 */
package com.google.code.laserswarm.Desim.BRDFcalc;

import java.util.LinkedList;
import java.util.Map;

import javax.vecmath.Vector3d;

import com.google.common.collect.Lists;

/**
 * @author Administrator
 * 
 */
public class Region {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Boundries bound = new Boundries();
		double rad = Math.PI / 180;
		Vector3d vctr = new Vector3d(0.9, 0.0, 0.1);
		bound.setLatLonBound(0, 20 * rad, 0, 20 * rad);
		Region area = new Region(bound);
		System.out.println(area.contains(vctr));
	}

	BRDF					brdf;
	Boundries				bound;
	boolean					hasAll;
	boolean					hasNone;

	LinkedList<Vector3d>	path;		// List of points in ECEF over which the brdf was determined

	public Region(boolean empty) {
		this.brdf = new BRDF(false, false); // create non symetric brdf
		this.path = Lists.newLinkedList();
		
		if (empty) {
			hasAll = false;
			hasNone = true;
		} else {
			hasAll = true;
			hasNone = false;
		}

	}

	public Region(Boundries bnd) {
		this.brdf = new BRDF(false, false); // create non symetric brdf
		this.path = Lists.newLinkedList();
		this.hasAll = false;
		this.hasNone = false;
		this.bound = bnd;

	}

	/**
	 * 
	 * @param recMap
	 *            Map of the receivers relative to the origin of the footprint and the amount of photons
	 * @param loc
	 *            Location pointing to the origin of the footprint in ECEF
	 * @param emVec
	 *            Position of the emitter relative to the base of the footprint ;
	 * @param atSl
	 *            Along track slope
	 * @param otSl
	 *            Off track slope
	 * @param trDir
	 *            Vector in ENU indicating the direction of movement of the emitter (along track vector)
	 */
	public void add(Map<Vector3d, Integer> recMap, Vector3d emVec, double atSl, double otSl,
			Vector3d trDir) {
		Vector3d em = new Vector3d(0.0, 0.0, 1);
		brdf.add(recMap, em, atSl, otSl, trDir);
		path.add(emVec);
	}

	public boolean contains(Vector3d vec) {

		if (hasAll)
			return true;
		if (hasNone)
			return false;
		return bound.contains(vec);
	}

}
