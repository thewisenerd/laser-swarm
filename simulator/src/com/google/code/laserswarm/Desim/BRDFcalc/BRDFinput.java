/**
 * 
 */
package com.google.code.laserswarm.Desim.BRDFcalc;

import java.util.Map;

import javax.vecmath.Vector3d;

/**
 * Class that contains the information needed by the FindBRDF class. All information corresponds to a
 * single instance in time, when the emitter emitted a certain pulse. This is also known as an interpulse
 * window.
 * 
 */
public class BRDFinput {
	/**
	 * 
	 * @return Returns the point on the Earth's surface directly below the emitter, in ECEF.
	 */
	public Vector3d getEmPos() {
		return emPos;
	}

	/**
	 * 
	 * @return Returns the direction in which the emitter is flying, in ECEF, normalized to one.
	 */
	public Vector3d getEmDir() {
		return emDir;
	}

	/**
	 * 
	 * @return Returns the along-track slope of the terrain, dz/dx.
	 */
	public double getAlongSlope() {
		return alongSlope;
	}

	/**
	 * 
	 * @return Returns the cross-track slope of the terrain, dz/dy.
	 */
	public double getOffSlope() {
		return offSlope;
	}

	/**
	 * 
	 * @return Returns a map keyed by receiver position when receiving a certain photon; value is the
	 *         number of photons received when at that particular position.
	 */
	public Map<Vector3d, Integer> getRecVecsECI() {
		return recVecsECI;
	}

	/**
	 * 
	 * @return Returns the time at which the pulse under consideration was emitted.
	 */
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
}
