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
	BRDF brdf;
	Boundries bound; 
	boolean hasAll;
	boolean hasNone;
	LinkedList<Vector3d> path;		//List of points in ECEF
	
	public Region(Boundries bnd) {
		this.brdf = new BRDF(false); // create non symetric brdf 
		this.path = Lists.newLinkedList();
		this.hasAll = false;
		this.hasNone = true;
		this.bound = bnd;	
		
	}
	public Region(boolean empty) {
		this.brdf = new BRDF(false); // create non symetric brdf 
		this.path = Lists.newLinkedList();
		
		hasAll = false;
		hasNone = true;
	}
	public void add(Map<Vector3d,Double> recMap, Vector3d loc) {
	brdf.add(recMap);
	path.add(loc);
	}
	
	public	boolean contains(Vector3d vec) {
		if(hasAll) return true;
		if(hasNone) return false;
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
