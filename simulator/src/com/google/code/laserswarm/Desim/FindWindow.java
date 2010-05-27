package com.google.code.laserswarm.Desim;

import javax.vecmath.Vector3d;

import com.google.code.laserswarm.conf.Configuration;

public class FindWindow {

	
	/**
	 * @param REC
	 * Vector3d position of the receiver
	 * @param EMIT
	 * Vector3d position of the emitter
	 */
	
	public double[] getWindow(Vector3d REC, Vector3d EMIT) {
		double maxDist;
		double minDist;
		
		Vector3d  maxGrndHght  = new Vector3d(EMIT);
		Vector3d  minGrndHght  = new Vector3d(EMIT);
		
		Vector3d  maxEmitHght  = new Vector3d(EMIT);
		Vector3d  minEmitHght  = new Vector3d(EMIT);
		
		Vector3d  maxRecHght	= new Vector3d(REC);
		Vector3d  minRecHght	= new Vector3d(REC);
		
		minGrndHght.normalize();
		maxGrndHght.normalize();
		maxGrndHght.scale(Configuration.R0+9E3);
		minGrndHght.scale(Configuration.R0-5E2);
		
		
		maxRecHght.sub(maxGrndHght);
		minRecHght.sub(minGrndHght);
		
		maxEmitHght.sub(maxGrndHght);
		minEmitHght.sub(minGrndHght);
		
		maxDist = maxRecHght.length()+maxEmitHght.length();
		minDist = minRecHght.length()+minEmitHght.length();
		
		return new double[] {maxDist/Configuration.c ,minDist/Configuration.c};  
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 
		throw new UnsupportedOperationException();
	}

}
