package com.google.code.laserswarm.simulation.postSimulation;

import static com.google.code.laserswarm.math.VectorMath.relative;

import java.util.HashMap;
import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.apache.commons.math.MathException;

import com.db4o.ObjectContainer;
import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.math.EllipticalArea;
import com.google.code.laserswarm.simulation.SimVars;
import com.google.code.laserswarm.simulation.Simulator;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class SlopeSpread implements IPostSimulation {

	private static final Logger	logger	= Logger.get(SlopeSpread.class);

	@Override
	public Simulator modify(Simulator simulation, Constellation constellation) {
		logger.inf("Post-process: Slope spreading");

		ObjectContainer newDb = Simulator.mkDb(simulation, "afterSlope");

		List<SimVars> data = simulation.getDataPoints();
		for (SimVars simVar : data) {
			// logger.dbg("Processing point a t=%f", simVar.t0);
			Vector3d normal = simVar.surfNormal;
			if (normal.z == 1 && normal.lengthSquared() == 1) {
				newDb.store(simVar);
				continue;
			}

			double terrainAngle = normal.angle(new Vector3d(0, 0, 1));
			double satDistance = relative(simVar.p0, simVar.pR).length();

			double alpha = constellation.getEmitter().getBeamDivergence();
			double beta = Math.PI - alpha - ((Math.PI / 2) - terrainAngle);
			double a = (Math.sin(alpha) / Math.sin(beta)) * satDistance;
			double b = alpha * satDistance;

			EllipticalArea area = new EllipticalArea(a, b);

			HashMap<Double, SimVars> newSimVars = Maps.newHashMap();
			for (Satellite sat : simVar.photonsE.keySet()) {
				// logger.inf("Working on satillite %s", sat);
				for (int photon = 0; photon < simVar.photonsE.get(sat); photon++) {
					double offset = 0;
					try {
						offset = area.cumulativeProbability(Math.random());// in m
					} catch (MathException e) {
						logger.wrn("Failed to compute offset for ellipse (a=%s and b=%s)", a, b);
					}

					// if (offset == 0) {
					// newDb.store(simVar);
					// continue;
					// }
					double dH = Math.sin(terrainAngle) * offset;
					double dt = dH / Configuration.c;

					final Double tRNew = simVar.tR + dt;
					final Double tENew = simVar.tE.get(sat) + dt;

					SimVars simVarNew;
					if (newSimVars.containsKey(tRNew))
						simVarNew = newSimVars.get(tRNew);
					else
						simVarNew = new SimVars(simVar);

					int photonsOld = 0;
					if (simVarNew.photonsE.containsKey(sat))
						photonsOld = simVar.photonsE.get(sat);

					simVarNew.apply(simVar, sat);
					simVarNew.photonsE.put(sat, photonsOld + 1);
					simVarNew.tR = tRNew;
					Vector3d dir = new Vector3d(simVar.pR);
					dir.normalize();
					dir.scale(new Vector3d(simVar.pR).length() + dH);
					simVarNew.pR = new Point3d(dir);
					simVarNew.tE.put(sat, tENew);
					newSimVars.put(tRNew, simVarNew);

					if (false) {
						logger.dbg("\n\nNEW PHOTON");
						logger.dbg("dH=>%s\tdt=%s", dH, dt);
						logger.dbg("normal=>%s", simVar.surfNormal);
						logger.dbg("TR from %s => %s", simVar.tR, tRNew);
						logger.dbg("TE from %s => %s", simVar.tE.get(sat), tENew);
					}
				}

			}

			int p1 = 0;
			for (Satellite sat : simVar.tE.keySet()) {
				p1 += simVar.photonsE.get(sat);
			}

			int p2 = 0;
			for (SimVars simVarNew : newSimVars.values()) {
				newDb.store(simVarNew);
				for (Satellite sat : simVarNew.tE.keySet()) {
					p2 += simVarNew.photonsE.get(sat);
				}
			}
			logger.dbg("Photons %d => %d (spread over: %d vars)", p1, p2, newSimVars.size());
		}

		newDb.commit();

		simulation.getDataPointsDB().close();// Close the old db
		simulation.setDb(newDb); // Set the new db
		logger.inf("Done slope spreading");

		return simulation;
	}
}
