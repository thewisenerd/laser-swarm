package com.google.code.laserswarm.simulation;

import jat.cm.Constants;
import jat.cm.KeplerElements;
import jat.spacetime.Time;

import java.util.HashMap;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.operation.projection.PointOutsideEnvelopeException;

import com.google.code.laserswarm.Orbit.OrbitClass;
import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.earthModel.Atmosphere;
import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.earthModel.ScatteringCharacteristics;
import com.google.code.laserswarm.earthModel.ScatteringParam;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class Simulator implements Runnable {

	private SimTemplate			template;
	private Thread				thread;
	private ElevationModel		earth;

	private static final Logger	logger	= Logger.get(Simulator.class);

	public Simulator(SimTemplate templ, ElevationModel earth) {
		this.template = templ;
		this.earth = earth;
	}

	public Thread getThread() {
		return thread;
	}

	@Override
	public void run() {
		double T0 = 0;
		double TE = 0;

		/* Make a list start times */
		Configuration config = template.getConfig();
		Constellation constalation = config.getConstellations().iterator().next();
		double f = constalation.getPulseFrequency();
		double dt = (1 / f);
		double ePhoton = (Constants.c * 6.62606896E-34) / constalation.getLaserWaveLength();

		int samples = (int) Math.ceil((TE - T0) / f);

		KeplerElements k = constalation.getEmitter().getKeplerElements();
		OrbitClass emittorOrbit = new OrbitClass(new Time(0), k);
		HashMap<Satellite, OrbitClass> receiverOrbits = Maps.newHashMap();
		for (Satellite sat : constalation.getReceivers()) {
			k = sat.getKeplerElements();
			OrbitClass o = new OrbitClass(new Time(0), k);
			receiverOrbits.put(sat, o);
		}

		for (int i = 0; i < samples; i++) {
			SimVars simVals = new SimVars();

			/* Start time */
			simVals.t0 = (i * dt + T0);

			/* Find the position of the constelation at that time */
			simVals.p0 = emittorOrbit.ECEF_point();
			emittorOrbit.propogate(dt);
			simVals.pE = Maps.newHashMap();
			for (Satellite sat : constalation.getReceivers()) {
				OrbitClass o = receiverOrbits.get(sat);
				simVals.pE.put(sat, o.ECEF_point());
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
				logger.wrn(e, "Point at t=%d is out of the dem grid", simVals.t0);
				continue;
			}
			Vector3d dR = new Vector3d(simVals.pR);
			dR.sub(simVals.p0);
			simVals.tR = dR.length() / Constants.c;
			Vector3d surfNormal = earth.getSurfaceNormal(new DirectPosition2D(
					sphere.z * (180 / Math.PI), sphere.y * (180 / Math.PI)));

			/* Make pulses (with downtravel) */
			simVals.power0 = constalation.getPower();
			double angle = dR.angle(new Vector3d(simVals.pR));
			simVals.powerR = Atmosphere.getInstance().computeIntesity(simVals.power0, angle);

			/* Make scatter characteristics */
			angle = Math.acos(dR.dot(surfNormal)) / (dR.length() * surfNormal.length());
			double z = dR.length() * Math.cos(angle);
			double x = dR.length() * Math.sin(angle);
			Vector3d incidence = new Vector3d(x, 0, z);
			ScatteringParam testParam = new ScatteringParam(2.4, 1, 1);
			simVals.scatter = new ScatteringCharacteristics(incidence, testParam);

			/* Compute scatter power per sat */
			for (Satellite sat : constalation.getReceivers()) {
				dR = new Vector3d(simVals.pE.get(sat));
				dR.sub(simVals.pR);
				angle = Math.acos(dR.dot(surfNormal)) / (dR.length() * surfNormal.length());
				z = dR.length() * Math.cos(angle);
				x = dR.length() * Math.sin(angle);
				Vector3d exittanceVector = new Vector3d(x, 0, z);
				simVals.powerR_SC.put(sat, simVals.scatter.probability(exittanceVector));
			}

			/* Travel up through atm */
			for (Satellite sat : constalation.getReceivers()) {
				dR = new Vector3d(simVals.pE.get(sat));
				dR.sub(simVals.pR);
				angle = dR.angle(new Vector3d(simVals.pR));
				simVals.powerE.put(sat, Atmosphere.getInstance().computeIntesity(
						simVals.powerR_SC.get(sat), angle));
				simVals.tE.put(sat, dR.length() / Constants.c);
			}

			/* Power/photons per receiver */
			for (Satellite sat : constalation.getReceivers()) {
				Double powerReceived = simVals.powerE.get(sat);
				double energy = powerReceived * dt;
				simVals.photonDensity.put(sat, energy / ePhoton);
				int nrP = (int) Math.floor(energy / ePhoton);
				if (Math.random() < (energy / ePhoton) - nrP)
					nrP++;
				simVals.photonsE.put(sat, nrP);
			}

		}

		throw new UnsupportedOperationException();
	}

	public Thread start() {
		thread = new Thread(this, "Simulator");
		thread.start();
		return thread;
	}

}
