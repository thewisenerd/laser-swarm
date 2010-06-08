/**
 * 
 */
package com.google.code.laserswarm.Desim.BRDFcalc;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3d;

import com.google.code.laserswarm.Orbit.OrbConv;
import com.google.code.laserswarm.math.Convert;
import com.google.code.laserswarm.math.Distribution;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

/**
 * BRDFs point north by default
 * 
 * @author Administrator
 *
 */

public class FindBRDF {
	Region defRegion;
	List<Region> region;
	OrbConv conv;
	
	private static final Logger	logger	= Logger.get(FindBRDF.class);

	public FindBRDF() {
		conv = new OrbConv();
		defRegion = new Region(false);
		region = Lists.newArrayList();
		//region.add();
	}
	/**
	 * from reflector coordinates 
	 * @param emVec emitter pos in ECEF
	 * @param recVecs vectors ENU
	 * @param emDir	Not needed
	 * @param reg
	 */
	private void putDataToRegion(Vector3d emVec, Map<Vector3d,Double> recVecs ){
	Region reg = selectRegion(emVec);	//select region
		reg.add(recVecs, emVec);
		
	}

	/**
	 * Selects the required region from the list to store the BRDF
	 * @param emVec emitter vector
	 * @return
	 */
	private Region selectRegion(Vector3d emVec) {
		for (Region regIt : region) {
			if(regIt.contains(emVec)) return regIt;
		}
		logger.dbg("returning default regoin");
		return defRegion;
		
		
	}
	/**
	 * 
	 * @param emVec ECI vector for emitter with altitude subtracted (points to the base of the plane )
	 * @param recVecs ECI vector for receivers 
	 */
	
	public void add(Vector3d emVecECI, Map<Vector3d,Double> recVecsECI, double t_cur ) {
		//convert emVec to ECEF
		//convert recVecs to ECEF to ENU see http://en.wikipedia.org/wiki/Geodetic_system#From_ECEF_to_ENU
		Map<Vector3d,Double> recVecsECU = Maps.newHashMap();
		Vector3d emVecECEF = conv.ECEF_vec(t_cur, emVecECI);		//convert to ECEF
		
		for (Vector3d recIt : recVecsECI.keySet()) {
		 Vector3d hm = Convert.toENU(conv.ECEF_vec(t_cur, recIt));
			recVecsECU.put(hm, recVecsECI.get(recIt)); //CONVERT to ECU
			
		}
		
		putDataToRegion(emVecECEF,recVecsECU);
		
				
		
		

		

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
