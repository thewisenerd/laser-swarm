package com.google.code.laserswarm.simulation;

import static com.google.code.laserswarm.math.VectorMath.relative;
import jat.cm.Constants;
import jat.cm.KeplerElements;
import jat.spacetime.Time;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.operation.projection.PointOutsideEnvelopeException;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.coverage.PointOutsideCoverageException;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Query;
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
import com.google.code.laserswarm.util.NonVolatileList;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

/**
 * 
 * Run the simulation based on a simulation template
 * <p>
 * Computes the actual signal propagation in multiple steps.
 * <ul>
 * <li>Creation at the emittor</li>
 * <li>Downtravel trough the atmosphere</li>
 * <li>Reflection in the footprint area</li>
 * <li>Uptravel trough the atmosphere</li>
 * <li>Detection by the receiver</li>
 * </ul>
 * <p/>
 * 
 * 
 * @author simon
 * 
 */
public class Simulator implements Runnable {

	private static final Logger	logger	= Logger.get(Simulator.class);

	public static long			timeOut	= (6 * 60 * 60000);			// ms
	private static boolean		usedDB	= true;

	private SimTemplate			template;
	private Thread				thread;
	private EarthModel			earth;

	private List<SimVars>		dataPoints;

	private double				powerPerPulse;

	private double				ePhoton;
	private ObjectContainer		db;
	private String				databaseFile;

	/**
	 * Make a new simulation from a given template over the given model of the earth
	 * <p>
	 * Simulates a given start time T0 to the end time TE in [s] (in the template)
	 * </p>
	 * 
	 * @param templ
	 *            Template that contains the basic simulation info
	 * @param earth
	 *            Earth model to use
	 */
	public Simulator(SimTemplate templ, EarthModel earth) {
		this.template = templ;
		this.earth = earth;

		File dbFile = new File(Configuration.volitileCache, toString() + "-" + hashCode() + ".db4o");
		if (!dbFile.exists())
			try {
				dbFile.createNewFile();
			} catch (IOException e) {
				logger.err(e, "Could not create the simvals db");
			}
		databaseFile = dbFile.getAbsolutePath();

		open();
	}

	public ObjectContainer getDataPointsDB() {
		return db;
	}

	public List<SimVars> getDataPoints() {
		if (usedDB) {
			Query query = db.query();
			query.constrain(SimVars.class);
			query.descend("t0").orderAscending();
			ObjectSet<SimVars> result = query.execute();
			return result;
		} else
			return dataPoints;
	}

	public Thread getThread() {
		return thread;
	}

	private SimVars mkSimPoint(long i, double timeStep, OrbitClass emittorOrbit,
			HashMap<Satellite, OrbitClass> receiverOrbits) {
		SimVars simVals = new SimVars();
		Constellation constellation = template.getConstellation();

		/* Start time */
		simVals.t0 = (i * timeStep + template.getT0());

		/* Find the position of the constellation at that time */
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
		if (Configuration.hasAction(Actions.CONSTANT_SCATTER))
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
		ePhoton = (Constants.c * 6.62606896E-34) / constellation.getLaserWaveLength();

		if (constellation.getPulselength() * constellation.getPulseFrequency() > 1)
			throw new RuntimeException("Cannot have a duty cycle of over 100% (of the laser)");
		powerPerPulse = constellation.getPower()
				/ (constellation.getPulselength() * constellation.getPulseFrequency());

		long samples = (long) Math.ceil((template.getTE() - template.getT0()) / dt);

		logger.inf("Simulator info for constellation: %s \n"
				+ "Emitter\t| %s, lasing power: %s W \n"
				+ "Laser\t| waveLength: %s m, f: %s Hz, pulse interval: %s s\n"
				+ "\t| pulse time: %s s, pulse energy: %s µJ",//
				constellation,//
				constellation.getEmitter(), constellation.getPower(),//
				constellation.getLaserWaveLength(), f, dt,//
				constellation.getPulselength(), powerPerPulse * constellation.getPulselength() * 1E6);

		KeplerElements k = constellation.getEmitter().getKeplerElements();

		OrbitClass emittorOrbit = new OrbitClass(new Time(Configuration.epoch), k);
		emittorOrbit.propogate(template.getT0());
		HashMap<Satellite, OrbitClass> receiverOrbits = Maps.newHashMap();
		for (Satellite sat : constellation.getReceivers()) {
			k = sat.getKeplerElements();
			OrbitClass o = new OrbitClass(new Time(Configuration.epoch), k);
			o.propogate(template.getT0());
			receiverOrbits.put(sat, o);
		}

		if (usedDB)
			cleanDb();
		else {
			try {
				dataPoints = new NonVolatileList(new File(Configuration.volitileCache, //
						template.toString() + "-" + template.hashCode() + ".db"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Prospector prospector = new Prospector(emittorOrbit, receiverOrbits, earth, samples, dt);

		long i = 0;
		while (i < samples) {
			if (i % 5000 == 0) {
				db.commit();
				logger.dbg("Running sample %s of %s", i, samples);
			}
			SimVars simVal = mkSimPoint(i, dt, emittorOrbit, receiverOrbits);
			if (simVal == null) {
				if (Configuration.hasAction(Actions.PROSPECT)) {
					/* Find timestep to next flight-over */
					i += prospector.prospect(i);
				} else
					i++;
			} else {
				if (Configuration.hasAction(Actions.COUNT_ONLY))
					simVal.reduce();
				if (usedDB)
					db.store(simVal);
				else
					dataPoints.add(simVal);
				i++;
			}

			if (System.currentTimeMillis() - tStart > timeOut) {
				db.commit();
				logger.inf("Timout forced quit %s ms", System.currentTimeMillis() - tStart);
				break;
			}
		}

		if (usedDB) {
			db.commit();
			logger.inf("Found %s points", db.query(SimVars.class).size());
		} else
			logger.inf("Found %s points", dataPoints.size());
	}

	public Thread start() {
		thread = new Thread(this, "Sim - " + template.getConstellation());
		thread.start();
		return thread;
	}

	private void cleanDb() {
		ObjectSet<SimVars> result = db.query(SimVars.class);
		while (result.hasNext())
			db.delete(result.next());
	}

	private void open() {
		if (db == null)
			db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), databaseFile);
	}

	public void close() {
		if (db != null)
			db.close();
		db = null;
	}

	@Override
	public String toString() {
		return "Sim-" + template.toString();
	}
}
