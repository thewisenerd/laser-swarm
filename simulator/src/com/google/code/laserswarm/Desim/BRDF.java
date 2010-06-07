/**
 * 
 */
package com.google.code.laserswarm.Desim;

import java.util.Map;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import com.google.code.laserswarm.math.Distribution;

/**
 * @author Administrator
 *
 */
public class BRDF extends Distribution  {	
		
	Sphere brdf;			//sphere that represents the brdf 
	Vector3d direction; 	//direction of the origin
	Vector3d inDir;		//direction of incidence
	boolean symetric;		//the brdf is symetric around the zenith 
	
	
	public BRDF(boolean sym) {
	symetric = sym;
	}
	
	public void add(Map<Vector3d,Double> dirs ){
		for (Map.Entry<Vector3d, Double> vecDblIt : dirs.entrySet()) {
			brdf.put(vecDblIt.getKey(), vecDblIt.getValue());
			
		}
	}
	public void add(Vector3d dir, double num ) {
		brdf.put(dir, num);
		
	}
	public void add(Vector3d dir) {
		brdf.put(dir, 1.0);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 
		throw new UnsupportedOperationException();
	}
	@Override
	public double probability(Vector3d x) {
		Double tmp;
		try{
		 tmp =brdf.get(x);
	}catch(NullPointerException e){
		 tmp = (double) 0;
	}
	
		return	 tmp;
		
	}

}
