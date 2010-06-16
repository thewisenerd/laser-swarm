/**
 * 
 */
package com.google.code.laserswarm.Desim.brdf;

import static com.google.code.laserswarm.math.VectorMath.avgVector;
import static com.google.code.laserswarm.math.VectorMath.relative;

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
	 * Emitter position in ECEF minus the altitude (point on the surface)
	 */
	private Vector3d				emitterPosition;

	/**
	 * Emitter direction (where the emitter is flying to make use of the slopes)
	 */
	private Vector3d				emitterDirection;

	/**
	 * Along-track slope of the footprint with respect to height
	 */
	private double					alongTrackSlope;

	/**
	 * Off-track slope of the footprint with respect to height
	 */
	private double					crossTrackSlope;

	/**
	 * Map of positions (at the time of receiving) of receivers and number of non-noise photons received
	 * (positive real number). Vectors are in ECEF.
	 */
	private Map<Vector3d, Integer>	receiverPositions;

	/**
	 * time of the instance when the pulse was emitted
	 */
	private double					currentTime;

	public BRDFinput(BRDFinput input) {
		emitterPosition = input.emitterPosition;
		emitterDirection = input.emitterDirection;
		alongTrackSlope = input.alongTrackSlope;
		crossTrackSlope = input.crossTrackSlope;
		receiverPositions = input.receiverPositions;
		currentTime = input.currentTime;
	}

	public BRDFinput(Vector3d emPos, Vector3d emDir, double alongSlope, double offSlope,
			Map<Vector3d, Integer> recVecsECI, double curTime) {

		this.emitterPosition = emPos;
		this.emitterDirection = emDir;
		this.alongTrackSlope = alongSlope;
		this.crossTrackSlope = offSlope;
		this.receiverPositions = recVecsECI;
		this.currentTime = curTime;
	}

	@Override
	protected BRDFinput clone() {
		return new BRDFinput(this);
	}

	/**
	 * 
	 * @return Returns the along-track slope of the terrain, dz/dx.
	 */
	public double getAlongTrackSlope() {
		return alongTrackSlope;
	}

	/**
	 * 
	 * @return Returns the cross-track slope of the terrain, dz/dy.
	 */
	public double getCrossTrackSlope() {
		return crossTrackSlope;
	}

	/**
	 * 
	 * @return Returns the time at which the pulse under consideration was emitted.
	 */
	public double getCurrentTime() {
		return currentTime;
	}

	/**
	 * 
	 * @return Returns the direction in which the emitter is flying, in ECEF, normalized to one.
	 */
	public Vector3d getEmitterDirection() {
		return emitterDirection;
	}

	/**
	 * 
	 * @return Returns the point on the Earth's surface directly below the emitter, in ECEF.
	 */
	public Vector3d getEmitterPosition() {
		return emitterPosition;
	}

	/**
	 * 
	 * @return Returns a map keyed by receiver position when receiving a certain photon; value is the
	 *         number of photons received when at that particular position.
	 */
	public Map<Vector3d, Integer> getReceiverPositions() {
		return receiverPositions;
	}

	public Vector3d getScatterPoint() {
		// TODO Auto-generated method stub
		//
		throw new UnsupportedOperationException();
	}

	public Vector3d getTerrainNormal() {
		// TODO Auto-generated method stub
		// 
		throw new UnsupportedOperationException();
	}

	public void merge(BRDFinput brdFinput) {
		Vector3d scatterPoint2 = brdFinput.getScatterPoint();
		for (Vector3d receiverPos2 : brdFinput.getReceiverPositions().keySet()) {
			Integer photons = brdFinput.getReceiverPositions().get(receiverPos2);
			Vector3d position = relative(scatterPoint2, receiverPos2);
			position.add(getScatterPoint());
			receiverPositions.put(position, photons);
		}

		alongTrackSlope = (alongTrackSlope + brdFinput.getAlongTrackSlope()) / 2;
		crossTrackSlope = (crossTrackSlope + brdFinput.getCrossTrackSlope()) / 2;
		normal = avgVector(getTerrainNormal(), brdFinput.getTerrainNormal());
	}
}
