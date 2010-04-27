package com.google.code.laserswarm.simulation;

import jat.cm.Constants;
import jat.cm.KeplerElements;
import jat.spacetime.CalDate;
import jat.spacetime.Time;

import java.util.HashMap;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.operation.projection.PointOutsideEnvelopeException;

import com.google.code.laserswarm.Orbit.OrbitClass;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.earthModel.Atmosphere;
import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.earthModel.ScatteringCharacteristics;
import com.google.code.laserswarm.earthModel.ScatteringParam;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class Simulator implements Runnable {

	public static final CalDate	epoch	= new CalDate(2000, 1, 1, 0, 0, 0);
	private static final Logger	logger	= Logger.get(Simulator.class);

	private SimTemplate			template;
	private Thread				thread;
	private ElevationModel		earth;

	private List<SimVars>		dataPoints;

	public Simulator(SimTemplate templ, ElevationModel earth) {
		this.template = templ;
		this.earth = earth;
	}

	public List<SimVars> getDataPoints() {
		return dataPoints;
	}

	public Thread getThread() {
		return thread;
	}

	@Override
	public void run() {
		double T0 = 481597;
		double TE = 481603;

		/* Make a list start times */
		Constellation constalation = template.getConstellation();
		double f = constalation.getPulseFrequency();
		double dt = (1 / f);
		logger.dbg("f: %s, dt:%s", f, dt);
		double ePhoton = (Constants.c * 6.62606896E-34) / constalation.getLaserWaveLength();

		int samples = (int) Math.ceil((TE - T0) / dt);

		KeplerElements k = constalation.getEmitter().getKeplerElements();

		OrbitClass emittorOrbit = new OrbitClass(new Time(epoch), k);
		emittorOrbit.propogate(T0);
		HashMap<Satellite, OrbitClass> receiverOrbits = Maps.newHashMap();
		for (Satellite sat : constalation.getReceivers()) {
			k = sat.getKeplerElements();
			OrbitClass o = new OrbitClass(new Time(epoch), k);
			o.propogate(T0);
			receiverOrbits.put(sat, o);
		}

		dataPoints = Lists.newLinkedList();

		for (int i = 0; i < samples; i++) {
			// if (i % 100 == 0)
			// logger.dbg("Running sample %s of %s", i, samples);
			SimVars simVals = new SimVars();

			/* Start time */
			simVals.t0 = (i * dt + T0);

			/* Find the position of the constelation at that time */
			Point3d pos = emittorOrbit.ECEF_point();
			pos.scale(1E3);
			simVals.p0 = pos;
			emittorOrbit.propogate(dt);
			simVals.pE = Maps.newHashMap();
			for (Satellite sat : constalation.getReceivers()) {
				OrbitClass o = receiverOrbits.get(sat);
				pos = o.ECEF_point();
				pos.scale(1E3);
				simVals.pE.put(sat, pos);
				o.propogate(dt);
			}

			/* Find the intersection location and time */
			Point3d sphere = null;
			try {
				sphere = earth.getIntersecion(new Vector3d(simVals.p0), simVals.p0);
				simVals.pR = new Point3d(sphere.x * Math.sin(sphere.z) * Math.cos(sphere.y),//
						sphere.x * Math.sin(sphere.z) * Math.sin(sphere.y),//
						sphere.x * Math.cos(sphere.z));
			} catch (PointOutsideEnvelopeException e) {
				// if (i % 100 == 0)
				// logger.wrn(e, "Point at t=%s is out of the dem grid", simVals.t0);
				continue;
			}
			logger.dbg("Yey over point (s:%s |t:%s)", i, T0 + i * dt);
			Vector3d dR = new Vector3d(simVals.pR);
			dR.sub(simVals.p0);
			simVals.tR = dR.length() / Constants.c;
			Vector3d surfNormal = earth.getSurfaceNormal(new DirectPosition2D(
					sphere.z * (180 / Math.PI), sphere.y * (180 / Math.PI)));

			/* Make pulses (with downtravel) */
			simVals.power0 = constalation.getPower();
			double angle = Math.PI - dR.angle(new Vector3d(simVals.pR));
			double rFootprint = dR.length() * Math.tan(constalation.getBeamDivergence());
			simVals.powerR = Atmosphere.getInstance().computeIntesity(simVals.power0, angle)
					/ (rFootprint * Math.PI * Math.PI);

			/* Make scatter characteristics */
			angle = Math.acos((dR.dot(surfNormal)) / (dR.length() * surfNormal.length()));
			double z = dR.length() * Math.cos(angle);
			double x = dR.length() * Math.sin(angle);
			Vector3d incidence = new Vector3d(x, 0, z);
			ScatteringParam testParam = new ScatteringParam(2.4, 1, 0.8);
			simVals.scatter = new ScatteringCharacteristics(incidence, testParam);

			/* Compute scatter power per sat */
			simVals.powerR_SC = Maps.newHashMap();
			for (Satellite sat : constalation.getReceivers()) {
				dR = new Vector3d(simVals.pE.get(sat));
				dR.sub(simVals.pR);
				angle = Math.acos(((dR.dot(surfNormal)) / (dR.length() * surfNormal.length())));
				z = dR.length() * Math.cos(angle);
				x = dR.length() * Math.sin(angle);
				Vector3d exittanceVector = new Vector3d(x, 0, z);
				simVals.powerR_SC
						.put(sat, simVals.powerR * simVals.scatter.probability(exittanceVector));
			}

			/* Travel up through atm */
			simVals.tE = Maps.newHashMap();
			simVals.powerE = Maps.newHashMap();
			for (Satellite sat : constalation.getReceivers()) {
				dR = new Vector3d(simVals.pE.get(sat));
				dR.sub(simVals.pR);
				angle = dR.angle(new Vector3d(simVals.pR));
				simVals.tE.put(sat, dR.length() / Constants.c);

				simVals.powerE.put(sat, Atmosphere.getInstance().computeIntesity(
						simVals.powerR_SC.get(sat), angle));
			}

			/* Power/photons per receiver */
			simVals.photonsE = Maps.newHashMap();
			simVals.photonDensity = Maps.newHashMap();
			for (Satellite sat : constalation.getReceivers()) {
				Double powerReceived = simVals.powerE.get(sat);

				double energy = powerReceived * constalation.getPulselength();
				simVals.photonDensity.put(sat, energy / ePhoton);
				energy = energy * sat.getAperatureArea();
				int nrP = (int) Math.floor(energy / ePhoton);
				if (Math.random() < (energy / ePhoton) - nrP)
					nrP++;
				simVals.photonsE.put(sat, nrP);
			}

			dataPoints.add(simVals);
		}
		logger.inf("Found %s points", dataPoints.size());
	}

	public Thread start() {
		thread = new Thread(this, "Simulator");
		thread.start();
		return thread;
	}
}
