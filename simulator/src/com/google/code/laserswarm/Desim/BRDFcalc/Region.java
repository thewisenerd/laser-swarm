/**
 * 
 */
package com.google.code.laserswarm.Desim.BRDFcalc;

import java.util.LinkedList;
import java.util.Map;

import javax.vecmath.Vector3d;

/**
 * @author Administrator
 *
 */
public class Region {
	BRDF brdf;
	Boundries bound; 
	LinkedList<Vector3d> path;		//List of points in ECEF
	
	public void add(Map<Vector3d,Double> recMap, Vector3d loc) {
	brdf.add(recMap);
	path.add(loc);
	}
	
	public	boolean contains(Vector3d vec) {
		return bound.contains(vec);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 
		throw new UnsupportedOperationException();
	}

}
