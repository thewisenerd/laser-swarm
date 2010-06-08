/**
 * 
 */
package com.google.code.laserswarm.Desim.BRDFcalc;

import java.util.LinkedList;
import java.util.Map;

import javax.vecmath.Vector3d;

import com.google.code.laserswarm.Orbit.OrbConv;

/**
 * BRDFs point north by default
 * 
 * @author Administrator
 *
 */

public class FindBRDF {
	BRDF brdf;
	Region region;
	OrbConv conv;
	/**
	 * from reflector coordinates 
	 * @param emVec
	 * @param recVecs
	 * @param emDir
	 * @param reg
	 */
	private void putDataToRegion(Vector3d emVec, Map<Vector3d,Double> recVecs, Vector3d emDir,Region reg ){
	reg.add(recVecs, emVec);
		
	}
	/**
	 * Input ECI output reflector coordinates
	 * @param emVec
	 */
	private void convRefCoord(Vector3d emVec,Map<Vector3d,Double> recVecs, double alt) {
		
		
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
