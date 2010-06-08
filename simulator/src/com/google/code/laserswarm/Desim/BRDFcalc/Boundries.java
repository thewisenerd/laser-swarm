package com.google.code.laserswarm.Desim.BRDFcalc;

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
	
	public boolean contains(Vector3d pnt) {
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

}
