/**
 * 
 */
package com.google.code.laserswarm.Desim.BRDFcalc;

import java.util.Map;

import javax.vecmath.Vector3d;

/**
 * Class that contains the information needed by the FindBRDF class. All information corresponds to an
 * instance in time.
 * 
 * 
 */
public class BRDFinput {
	public Vector3d getEmPos() {
		return emPos;
	}

	public Vector3d getEmDir() {
		return emDir;
	}

	public double getAlongSlope() {
		return alongSlope;
	}

	public double getOffSlope() {
		return offSlope;
	}

	public Map<Vector3d, Integer> getRecVecsECI() {
		return recVecsECI;
	}

	public double getCurTime() {
		return curTime;
	}

	public BRDFinput(Vector3d emPos, Vector3d emDir, double alongSlope, double offSlope,
			Map<Vector3d, Integer> recVecsECI, double curTime) {

		this.emPos = emPos;
		this.emDir = emDir;
		this.alongSlope = alongSlope;
		this.offSlope = offSlope;
		this.recVecsECI = recVecsECI;
		this.curTime = curTime;
	}

	/**
	 * Emitter position in ECI minus the altitude (point on the surface)
	 */
	Vector3d				emPos;
	/**
	 * Emitter direction (where the emitter is flying to make use of the slopes)
	 */
	Vector3d				emDir;
	/**
	 * Along-track slope of the footprint with respect to height
	 */
	double					alongSlope;
	/**
	 * Off-track slope of the footprint with respect to height
	 */
	double					offSlope;
	/**
	 * Map of positions (at the time of receiving) of receivers and number of non-noise photons received
	 * (positive real number). Vectors are in ECI.
	 */
	Map<Vector3d, Integer>	recVecsECI;
	/**
	 * time of the instance when the pulse was emitted
	 */
	double					curTime;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// 
		throw new UnsupportedOperationException();
	}

}
