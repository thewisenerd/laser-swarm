/**
 * 
 */
package com.google.code.laserswarm.Desim.brdf;

import static com.google.code.laserswarm.math.Convert.toPoint;
import static com.google.code.laserswarm.math.Convert.toSphere;
import static com.google.code.laserswarm.math.Convert.toVector;
import static com.google.code.laserswarm.math.Convert.toXYZ;
import static com.google.code.laserswarm.math.VectorMath.ecefToEnu;
import static com.google.code.laserswarm.math.VectorMath.enuToEcef;
import static com.google.code.laserswarm.math.VectorMath.enuToLocal;
import static com.google.code.laserswarm.math.VectorMath.localToEnu;
import static com.google.code.laserswarm.math.VectorMath.relative;
import static com.google.code.laserswarm.math.VectorMath.rotate;

import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.math.Plane;
import org.geotools.referencing.operation.projection.PointOutsideEnvelopeException;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.lyndir.lhunath.lib.system.logging.Logger;

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

	/**
	 * The point where everything was scattered.
	 */
	private Vector3d				scatterPoint;

	private static final Logger		logger	= Logger.get(BRDFinput.class);

	public BRDFinput(BRDFinput input) {
		emitterPosition = input.emitterPosition;
		emitterDirection = input.emitterDirection;
		alongTrackSlope = input.alongTrackSlope;
		crossTrackSlope = input.crossTrackSlope;
		receiverPositions = input.receiverPositions;
		currentTime = input.currentTime;
		scatterPoint = input.scatterPoint;
	}

	public BRDFinput(Vector3d emPos, Vector3d emDir, Vector3d scatterPoint, double alongSlope,
			double offSlope, Map<Vector3d, Integer> recVecsECI, double curTime) {
		this.emitterPosition = emPos;
		this.emitterDirection = emDir;
		this.alongTrackSlope = alongSlope;
		this.crossTrackSlope = offSlope;
		this.receiverPositions = recVecsECI;
		this.currentTime = curTime;
		this.scatterPoint = scatterPoint;
	}

	@Override
	public BRDFinput clone() {
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

	private double getCrossTrackSlopeCorrected() {
		Vector3d pos = getScatterPoint();
		Vector3d dir = getEmitterDirection();
		Point3d posSphere = toSphere(pos);
		Point3d dirSphere = toSphere(dir);
		posSphere.scale(180 / Math.PI);
		dirSphere.scale(180 / Math.PI);

		double dAngle = 0.00000001;
		DirectPosition2D left = new DirectPosition2D(posSphere.y + dAngle * dirSphere.z,
				posSphere.z - dAngle * dirSphere.y);
		DirectPosition2D right = new DirectPosition2D(posSphere.y - dAngle * dirSphere.z,
				posSphere.z + dAngle * dirSphere.y);

		double hLeft = 0;
		double hRight = 0;
		try {
			EarthModel earth = EarthModel.getDefaultModel();
			hLeft = earth.getElevation(left) + Configuration.R0;
			hRight = earth.getElevation(right) + Configuration.R0;
		} catch (PointOutsideEnvelopeException e) {
			logger.wrn(e, "Boundary case prolly");
		}
		Point3d pLeft = toXYZ(new Point3d(Configuration.R0, left.x, left.y));
		Point3d pRight = toXYZ(new Point3d(Configuration.R0, right.x, right.y));
		double trueCrossTrackSlope = (hRight - hLeft) / pLeft.distance(pRight);

		double cross1 = trueCrossTrackSlope - crossTrackSlope;
		double cross2 = trueCrossTrackSlope + crossTrackSlope;
		if (cross1 < cross2)
			return crossTrackSlope;
		else
			return -crossTrackSlope;
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
		return scatterPoint;
	}

	public Vector3d getTerrainNormal() {
		double crossSlope = getCrossTrackSlopeCorrected();
		double alongSlope = getAlongTrackSlope();
		Vector3d dir = getEmitterDirection();
		Point3d dirSphere = toSphere(dir);

		Vector3d alongTrackP = toVector(dirSphere);
		alongTrackP.x = alongSlope;
		Vector3d crosstrackP = toVector(dirSphere);
		crosstrackP = rotate(crosstrackP, 1, -90 * Math.PI / 180);
		crosstrackP.x = crossSlope;

		Plane terrainPlane = new Plane();
		terrainPlane.setPlane(new Point3d(0, 0, 0),
								toPoint(alongTrackP),
								toPoint(crosstrackP));

		Vector3d dZ_dLON = new Vector3d(1, 0, terrainPlane.x(1, 0));
		Vector3d dZ_dLAT = new Vector3d(0, 1, terrainPlane.x(0, 1));

		Point3d sp = toSphere(scatterPoint);
		Vector3d nTrue = EarthModel.getDefaultModel().getSurfaceNormal(
				new DirectPosition2D(sp.y * 180 / Math.PI, sp.z * 180 / Math.PI));

		Vector3d n = new Vector3d();
		n.cross(dZ_dLON, dZ_dLAT);
		return nTrue;
	}

	public void merge(BRDFinput brdFinput) {
		Vector3d scatter = brdFinput.getScatterPoint();
		Point3d scatterSphere = toSphere(scatter);
		Vector3d scatter2 = brdFinput.getScatterPoint();
		Point3d scatterSphere2 = toSphere(scatter2);

		for (Vector3d receiverPos2 : brdFinput.getReceiverPositions().keySet()) {
			Integer photons = brdFinput.getReceiverPositions().get(receiverPos2);
			Vector3d position2 = relative(scatter2, receiverPos2);

			Vector3d enu2 = ecefToEnu(position2, scatterSphere2.y, scatterSphere2.z);
			Vector3d normal2 = brdFinput.getTerrainNormal();
			Vector3d local = enuToLocal(enu2, normal2);

			Vector3d normal = getTerrainNormal();
			Vector3d enu = localToEnu(local, normal);
			Vector3d ecef = enuToEcef(enu, scatterSphere.y, scatterSphere.z);
			ecef.add(getScatterPoint());
			receiverPositions.put(ecef, photons);
		}

		alongTrackSlope = (alongTrackSlope + brdFinput.getAlongTrackSlope()) / 2;
		crossTrackSlope = (crossTrackSlope + brdFinput.getCrossTrackSlope()) / 2;
	}
}
