package com.google.code.laserswarm.desim;

import java.util.List;


import javax.vecmath.Vector3d;

public class Boundries {
	//ECEF vector on earth
	List<Vector3d> pnts;
	
	public void setLatLonBound(double latMin, double latMax, double lonMin, double lonMax) {
		pnts.add(makeVec(latMin,lonMin));
		pnts.add(makeVec(latMin,lonMax));
		pnts.add(makeVec(latMax,lonMin));
		pnts.add(makeVec(latMax,lonMax));
	}
	private Vector3d makeVec(double lat, double lon){
		return new Vector3d(Math.cos(lat)*Math.cos(lon),Math.cos(lat)*Math.sin(lon),Math.sin(lat));
	}
	
	public void addConPoint(Vector3d pnt){
		pnts.add(pnt);
	}

}
