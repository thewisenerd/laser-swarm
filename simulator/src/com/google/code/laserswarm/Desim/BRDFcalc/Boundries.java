package com.google.code.laserswarm.Desim.BRDFcalc;

import java.util.List;


import javax.vecmath.Vector3d;

import com.google.code.laserswarm.SimulationTester;
import com.google.common.collect.Lists;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class Boundries {
	//ECEF vector on earth
	List<Vector3d> pnts;
	private static final Logger	logger	= Logger.get(Boundries.class);
	
	public Boundries() {
		pnts = Lists.newLinkedList();
		// TODO Auto-generated constructor stub
	}
	/**
	 * all arguments are in rad
	 * @param latMin 
	 * @param latMax
	 * @param lonMin
	 * @param lonMax
	 */
	public void setLatLonBound(double latMin, double latMax, double lonMin, double lonMax) {
		pnts.add(makeVec(latMin,lonMin));
		pnts.add(makeVec(latMin,lonMax));
		pnts.add(makeVec(latMax,lonMin));
		pnts.add(makeVec(latMax,lonMax));
	}
	private Vector3d makeVec(double lat, double lon){
		return new Vector3d(Math.cos(lat)*Math.cos(lon),Math.cos(lat)*Math.sin(lon),Math.sin(lat));
	}
	
	public boolean contains(Vector3d pnt) {
		 pnt.normalize(); 
			Double maxX = Double.NEGATIVE_INFINITY;
		Double minX = Double.POSITIVE_INFINITY;
		Double maxY = Double.NEGATIVE_INFINITY;
		Double minY = Double.POSITIVE_INFINITY;
		Double maxZ = Double.NEGATIVE_INFINITY;
		Double minZ = Double.POSITIVE_INFINITY;
		for (Vector3d pIt : pnts) {
			
			if(pIt.x > maxX) maxX = pIt.x; 
			if(pIt.x < minX ) minX   = pIt.x;
			if(pIt.x > maxY) maxY = pIt.y; 
			if(pIt.x < minY ) minY   = pIt.y;
			if(pIt.x > maxZ) maxZ = pIt.z; 
			if(pIt.x < minZ ) minZ   = pIt.z;
	}
		return(pnt.x <= maxX && pnt.x >= minX   && pnt.y <= maxY && pnt.y >= minY && pnt.z <= maxZ && pnt.z >= minZ);

	}
	public void addConPoint(Vector3d pnt){
		pnts.add(pnt);
	}

	public static void main(String[] args) {
		Boundries bnd = new Boundries();
		double rad = Math.PI/180;
		Vector3d pnt1 = new Vector3d(0.9396926207859084,.0,0.3420201433256687);
		bnd.setLatLonBound(0, 20*rad, 0, 20*rad);
		logger.dbg("points in the list: %s", bnd.pnts);
		logger.dbg("contains %s vector: %s", pnt1,bnd.contains(pnt1));
	}
}
