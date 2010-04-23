package com.google.code.laserswarm.simulation;

import jat.cm.Constants;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.geotools.referencing.operation.projection.PointOutsideEnvelopeException;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.earthModel.Atmosphere;
import com.google.code.laserswarm.earthModel.ElevationModel;
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

		for (int i = 0; i < samples; i++) {
			SimVars simVals = new SimVars();

			/* Start time */
			simVals.t0 = (i * dt + T0);

			/* Find the position of the constelation at that time */
			// TODO: fit in orbits
			simVals.p0 = new Point3d();
			simVals.pE = Maps.newHashMap();

			/* Find the intersection location and time */
			try {
				simVals.pR = earth.getIntersecion(new Vector3d(simVals.p0), simVals.p0);
			} catch (PointOutsideEnvelopeException e) {
				logger.wrn(e, "Point at t=%d is out of the dem grid", simVals.t0);
				continue;
			}
			Vector3d dR = new Vector3d(simVals.pR);
			dR.sub(simVals.p0);
			simVals.tR = dR.length() / Constants.c;

			/* Make pulses (with downtravel) */
			simVals.power0 = constalation.getPower();
			double angle = dR.angle(new Vector3d(simVals.pR));
			simVals.powerR = Atmosphere.getInstance().computeIntesity(simVals.power0, angle);

			/* Make scatter characteristics */
			// NOOOOOOO

			/* Compute scatter power per sat */
			// NOOOOOOO

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
