package com.google.code.laserswarm.simulation;

import static com.google.code.laserswarm.math.VectorMath.relative;
import jat.cm.Constants;
import jat.cm.KeplerElements;
import jat.spacetime.Time;

import java.util.HashMap;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.operation.projection.PointOutsideEnvelopeException;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.coverage.PointOutsideCoverageException;

import com.google.code.laserswarm.Orbit.OrbitClass;
import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.conf.Configuration.Actions;
import com.google.code.laserswarm.earthModel.Atmosphere;
import com.google.code.laserswarm.earthModel.Convert;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.earthModel.ScatteringCharacteristics;
import com.google.code.laserswarm.earthModel.ScatteringParam;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

/**
 * 
 * Run the simulation based pn a simulation template
 * 
 * @author simon
 * 
 */
public class Simulator implements Runnable {

	private static final Logger	logger	= Logger.get(Simulator.class);
	public static long			timeOut	= (6 * 60 * 60000);			// ms

	private SimTemplate			template;
	private Thread				thread;
	private EarthModel			earth;

	private double				T0		= 579267.5;
	private double				TE		= 579341.4;

	private List<SimVars>		dataPoints;

	private double				powerPerPulse;

	private double				ePhoton;

	public Simulator(SimTemplate templ, EarthModel earth) {
		this.template = templ;
		this.earth = earth;
		setTime(templ.getT0(), templ.getTE());
	}

	public Simulator(SimTemplate templ, EarthModel earth, double T0, double TE) {
		this.template = templ;
		this.earth = earth;
		setTime(T0, TE);
	}

	public List<SimVars> getDataPoints() {
		return dataPoints;
	}

	public double getT0() {
		return T0;
	}

	public double getTE() {
		return TE;
	}

	public Thread getThread() {
		return thread;
	}

	private SimVars mkSimPoint(long i, double timeStep, OrbitClass emittorOrbit,
			HashMap<Satellite, OrbitClass> receiverOrbits) {
		SimVars simVals = new SimVars();
		Constellation constellation = template.getConstellation();

		/* Start time */
		simVals.t0 = (i * timeStep + T0);

		/* Find the position of the constelation at that time */
		Point3d pos = emittorOrbit.ECEF_point();
		pos.scale(1E3);
		simVals.p0 = pos;
		emittorOrbit.propogate(timeStep);

		simVals.pE = Maps.newHashMap();
		for (Satellite sat : constellation.getReceivers()) {
			OrbitClass o = receiverOrbits.get(sat);
			pos = o.ECEF_point();
			pos.scale(1E3);
			simVals.pE.put(sat, pos);
			o.propogate(timeStep);
		}

		/* Find the intersection location and time */
		Point3d sphere = null;
		try {
			sphere = earth.getIntersection(new Vector3d(simVals.p0), simVals.p0);
			simVals.pR = Convert.toXYZ(sphere);
		} catch (PointOutsideEnvelopeException e) {
			// if (i % 100 == 0)
			// logger.wrn(e, "Point at t=%s is out of the dem grid", simVals.t0);
			return null;
		}
		// logger.dbg("Yey over point (s:%s |t:%s)", i, T0 + i * dt);
		Vector3d dR = relative(simVals.p0, simVals.pR);
		simVals.tR = dR.length() / Constants.c;
		DirectPosition2D reflectionPoint = new DirectPosition2D(sphere.y * (180 / Math.PI), sphere.z
				* (180 / Math.PI));
		Vector3d surfNormal = null;
		try {
			surfNormal = earth.getSurfaceNormal(reflectionPoint);
		} catch (PointOutsideCoverageException e) {
			logger.wrn(e, // 
					"Cannot find surf normal of sample %s (prolly border case) :", i, simVals);
			return null;
		}

		/* sun vector */
		simVals.sunVector = emittorOrbit.sunvec_ECEF();
		double sunAngle = simVals.sunVector.angle(new Vector3d(simVals.pR));
		simVals.illuminated = (sunAngle < Math.PI / 2);

		/* Make pulses (with downtravel) */
		simVals.power0 = powerPerPulse;
		double angle = Math.PI - dR.angle(new Vector3d(simVals.pR));
		// double rFootprint = dR.length() * Math.tan(constellation.getEmitter().getBeamDivergence());
		simVals.powerR = Atmosphere.getInstance().computeIntensity(simVals.power0, angle);

		/* Make scatter characteristics */
		angle = Math.acos((dR.dot(surfNormal)) / (dR.length() * surfNormal.length()));
		double z = dR.length() * Math.cos(angle);
		double x = dR.length() * Math.sin(angle);
		Vector3d incidence = new Vector3d(x, 0, z);
		ScatteringParam param;
		if (Configuration.getInstance().hasAction(Actions.CONSTANT_SCATTER))
			param = new ScatteringParam(1.5, 1.3, -0.5);
		else
			try {
				param = earth.getScatteringParam(reflectionPoint);
			} catch (CannotEvaluateException e) {
				logger.wrn(e, "Cannot find Scattering param of sample %s:", i, simVals);
				return null;
			}
		simVals.scatter = new ScatteringCharacteristics(incidence, param);

		/* Compute scatter power per sat */
		simVals.powerR_SC = Maps.newHashMap();
		for (Satellite sat : constellation.getReceivers()) {
			dR = relative(simVals.pR, simVals.pE.get(sat));
			angle = dR.angle(surfNormal);
			z = dR.length() * Math.cos(angle);
			x = dR.length() * Math.sin(angle);
			Vector3d exittanceVector = new Vector3d(x, 0, z);
			simVals.powerR_SC.put(sat, simVals.scatter.probability(exittanceVector) // 
					* simVals.powerR / dR.lengthSquared());
			// System.out.println(dR.lengthSquared());
		}

		/* Travel up through atm */
		simVals.tE = Maps.newHashMap();
		simVals.powerE = Maps.newHashMap();
		for (Satellite sat : constellation.getReceivers()) {
			dR = new Vector3d(simVals.pE.get(sat));
			dR.sub(simVals.pR);
			angle = dR.angle(new Vector3d(simVals.pR));
			simVals.tE.put(sat, dR.length() / Constants.c);

			simVals.powerE.put(sat, Atmosphere.getInstance().computeIntensity(
					simVals.powerR_SC.get(sat), angle));
		}

		/* Power/photons per receiver */
		simVals.photonsE = Maps.newHashMap();
		simVals.photonDensity = Maps.newHashMap();
		for (Satellite sat : constellation.getReceivers()) {
			Double powerReceived = simVals.powerE.get(sat);

			double energy = powerReceived * constellation.getPulselength();
			simVals.photonDensity.put(sat, energy / ePhoton);
			energy = energy * sat.getAperatureArea();
			int nrP = (int) Math.floor(constellation.getReceiverEfficiency() * energy / ePhoton);
			// logger.dbg("test: %s", (constellation.getReceiverEfficiency() * energy / ePhoton) - nrP);
			if (Math.random() < (constellation.getReceiverEfficiency() * energy / ePhoton) - nrP)
				nrP++;
			simVals.photonsE.put(sat, nrP);
		}
		return simVals;

	}

	@Override
	public void run() {
		long tStart = System.currentTimeMillis();

		Constellation constellation = template.getConstellation();
		double f = constellation.getPulseFrequency();
		double dt = (1 / f);
		logger.dbg("f: %s, dt:%s", f, dt);
		ePhoton = (Constants.c * 6.62606896E-34) / constellation.getLaserWaveLength();

		if (constellation.getPulselength() * constellation.getPulseFrequency() > 1)
			throw new RuntimeException("Cannot have a duty cycle of over 100% (of the laser)");
		powerPerPulse = constellation.getPower()
				/ (constellation.getPulselength() * constellation.getPulseFrequency());

		long samples = (long) Math.ceil((TE - T0) / dt);

		KeplerElements k = constellation.getEmitter().getKeplerElements();

		OrbitClass emittorOrbit = new OrbitClass(new Time(Configuration.epoch), k);
		emittorOrbit.propogate(T0);
		HashMap<Satellite, OrbitClass> receiverOrbits = Maps.newHashMap();
		for (Satellite sat : constellation.getReceivers()) {
			k = sat.getKeplerElements();
			OrbitClass o = new OrbitClass(new Time(Configuration.epoch), k);
			o.propogate(T0);
			receiverOrbits.put(sat, o);
		}

		dataPoints = Lists.newLinkedList();

		Prospector prospector = new Prospector(emittorOrbit, receiverOrbits, earth, samples, dt);

		long i = 0;
		while (i < samples) {
			if (i % 5000 == 0)
				logger.dbg("Running sample %s of %s", i, samples);
			SimVars simVal = mkSimPoint(i, dt, emittorOrbit, receiverOrbits);
			if (simVal == null) {
				if (Configuration.getInstance().hasAction(Actions.PROSPECT)) {
					/* Find timestep to next flight-over */
					i += prospector.prospect(i);
				} else
					i++;
			} else {
				if (Configuration.getInstance().hasAction(Actions.COUNT_ONLY))
					simVal.reduce();
				dataPoints.add(simVal);
				i++;
			}

			if (System.currentTimeMillis() - tStart > timeOut) {
				logger.inf("Timout forced quit %s ms", System.currentTimeMillis() - tStart);
				break;
			}
		}
		logger.inf("Found %s points", dataPoints.size());
	}

	private void setTime(double T0, double TE) {
		this.T0 = T0;
		this.TE = TE;
	}

	public Thread start() {
		thread = new Thread(this, "Sim - " + template.getConstellation());
		thread.start();
		return thread;
	}
}
