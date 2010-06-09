package com.google.code.laserswarm.simulation;

import static com.google.code.laserswarm.math.VectorMath.ecefToEnu;
import static com.google.code.laserswarm.math.VectorMath.enuToLocal;
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
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.earthModel.ScatteringCharacteristics;
import com.google.code.laserswarm.earthModel.ScatteringParam;
import com.google.code.laserswarm.math.Convert;
import com.google.code.laserswarm.util.NonVolatileList;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

/**
 * 
 * Run the simulation based on a simulation template
 * <p>
 * Computes the actual signal propagation in multiple steps.
 * <ul>
 * <li>Creation at the emitter</li>
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

	public static ObjectContainer mkDb(Simulator sim, String sufix) {
		File dbFile = new File(Configuration.volatileCache, sim.toString()
				+ (sufix.equals("") ? "" : "-" + sufix)
				+ "-" + sim.hashCode() + ".db4o");
		if (!dbFile.exists())
			try {
				dbFile.createNewFile();
			} catch (IOException e) {
				logger.err(e, "Could not create the simvals db");
			}
		return Db4oEmbedded.openFile(dbFile.getAbsolutePath());
	}

	private SimTemplate		template;
	private Thread			thread;

	private EarthModel		earth;

	private List<SimVars>	dataPoints;

	private double			powerPerPulse;
	private double			ePhoton;
	private ObjectContainer	db;

	private String			databaseFile;

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

		this.db = mkDb(this, "");
		open();
	}

	private void cleanDb() {
		ObjectSet<SimVars> result = db.query(SimVars.class);
		while (result.hasNext())
			db.delete(result.next());
	}

	public void close() {
		if (db != null)
			db.close();
		db = null;
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

	public ObjectContainer getDataPointsDB() {
		return db;
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
			// logger.dbg(e, "Point at t=%s is out of the dem grid", simVals.t0);
			return null;
		}
		// logger.dbg("Yey over point (s:%s |t:%s)", i, T0 + i * dt);
		Vector3d dR = relative(simVals.p0, simVals.pR);
		simVals.tR = dR.length() / Constants.c;
		DirectPosition2D reflectionPoint = new DirectPosition2D(sphere.y * (180 / Math.PI), sphere.z
				* (180 / Math.PI));
		simVals.surfNormal = null;
		try {
			simVals.surfNormal = earth.getSurfaceNormal(reflectionPoint);
		} catch (PointOutsideCoverageException e) {
			logger.dbg(e, // 
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
		// angle = Math.acos((dR.dot(simVals.surfNormal)) / (dR.length() * simVals.surfNormal.length()));
		// double z = dR.length() * Math.cos(angle);
		// double x = dR.length() * Math.sin(angle);
		// Vector3d incidence = new Vector3d(x, 0, z);
		Vector3d incidence = ecefToEnu(relative(simVals.pR, simVals.p0), sphere.y, sphere.z);
		incidence = enuToLocal(incidence, simVals.surfNormal);
		incidence.normalize();
		ScatteringParam param;
		if (Configuration.hasAction(Actions.CONSTANT_SCATTER))
			param = new ScatteringParam(1.5, 1, 0);
		else
			try {
				param = earth.getScatteringParam(reflectionPoint);
			} catch (CannotEvaluateException e) {
				logger.dbg(e, "Cannot find Scattering param of sample %s:", i, simVals);
				return null;
			}
		simVals.scatter = new ScatteringCharacteristics(incidence, param);

		/* Compute scatter power per sat */
		simVals.powerR_SC = Maps.newHashMap();
		for (Satellite sat : constellation.getReceivers()) {
			dR = relative(simVals.pR, simVals.pE.get(sat));
			// logger.wrn("Sat=%s", sat);
			// logger.wrn("%s", dR);
			// logger.wrn("sphre: %s", Convert.toSphere(dR));
			// angle = dR.angle(simVals.surfNormal);
			// double z = dR.length() * Math.cos(angle);
			// double x = dR.length() * Math.sin(angle);
			// Vector3d exittanceVector = new Vector3d(x, 0, z);
			Vector3d exittanceVector = ecefToEnu(relative(simVals.pR, simVals.pE.get(sat)),
					sphere.y, sphere.z);
			exittanceVector = enuToLocal(exittanceVector, simVals.surfNormal);
			exittanceVector.normalize();
			simVals.powerR_SC.put(sat, simVals.scatter.probability(exittanceVector) // 
					* simVals.powerR / dR.lengthSquared()); // FIXME: Number hack
			// System.out.println(dR.lengthSquared());
		}

		/* Travel up through atm */
		simVals.tE = Maps.newHashMap();
		simVals.powerE = Maps.newHashMap();
		for (Satellite sat : constellation.getReceivers()) {
			dR = relative(simVals.pR, simVals.pE.get(sat));
			angle = dR.angle(new Vector3d(simVals.pR));
			if (angle > (40 * Math.PI / 180))
				System.out.println("Woops : ) Sat to far");
			simVals.tE.put(sat, dR.length() / Constants.c);
			// logger.wrn("Angle (%s) = %f", sat, angle * 180 / Math.PI);
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

	private void open() {
		if (db == null)
			db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), databaseFile);
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
				+ "\t| pulse time: %s s, pulse energy: %s �J",//
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
				dataPoints = new NonVolatileList(new File(Configuration.volatileCache, //
						template.toString() + "-" + template.hashCode() + ".db"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Prospector prospector = Prospector.getFactory().getProspector(
				emittorOrbit, receiverOrbits, earth, samples, dt);
		if (!template.useTime())
			prospector.setSamples(Long.MAX_VALUE);

		long i = 0;
		boolean goalReached = false;
		while (!goalReached) {
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

			/* Force quit after timeout */
			if (System.currentTimeMillis() - tStart > timeOut) {
				db.commit();
				logger.inf("Timout forced quit %s ms", System.currentTimeMillis() - tStart);
				goalReached = true;
			}

			/* Check if the end of the loop conditions was reached */
			if (template.useTime()) {
				if (i >= samples)
					goalReached = true;
			} else {
				if (usedDB) {
					if (db.query(SimVars.class).size() >= template.getSamples())
						goalReached = true;
				} else if (dataPoints.size() >= template.getSamples())
					goalReached = true;
			}
		}
		if (usedDB) {
			db.commit();
			logger.inf("Found %s points", db.query(SimVars.class).size());
		} else
			logger.inf("Found %s points", dataPoints.size());

	}

	public void setDb(ObjectContainer db) {
		this.db = db;
	}

	public Thread start() {
		thread = new Thread(this, "Sim - " + template.getConstellation());
		thread.start();
		return thread;
	}

	@Override
	public String toString() {
		return "Sim-" + template.toString();
	}
}
