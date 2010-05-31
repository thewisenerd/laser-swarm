package com.google.code.laserswarm.simulation.postSimulation;

import static com.google.code.laserswarm.math.VectorMath.relative;

import java.util.List;

import javax.vecmath.Vector3d;

import org.apache.commons.math.MathException;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Predicate;
import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.math.EllipticalArea;
import com.google.code.laserswarm.simulation.SimVars;
import com.google.code.laserswarm.simulation.Simulator;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class SlopeSpread implements IPostSimulation {

	private static final Logger	logger	= Logger.get(SlopeSpread.class);

	@Override
	public Simulator modify(Simulator simulation, Constellation constellation) {
		logger.inf("Post-process: Slope spreading");

		ObjectContainer newDb = simulation.mkDb(simulation, "afterSlope");

		List<SimVars> data = simulation.getDataPoints();
		for (SimVars simVar : data) {
			Vector3d normal = simVar.surfNormal;
			if (normal.z == 1)
				continue;

			double terrainAngle = normal.angle(new Vector3d(0, 0, 1));
			double satDistance = relative(simVar.p0, simVar.pR).length();

			double alpha = constellation.getEmitter().getBeamDivergence();
			double beta = Math.PI - alpha - (Math.PI - terrainAngle);
			double a = (Math.sin(alpha) / Math.sin(beta)) * satDistance;
			double b = alpha * satDistance;

			EllipticalArea area = new EllipticalArea(a, b);

			for (Satellite sat : simVar.photonsE.keySet()) {
				for (int photon = 0; photon < simVar.photonsE.get(sat); photon++) {
					double offset = 0;
					try {
						offset = area.cumulativeProbability(Math.random());// in m
					} catch (MathException e) {
						logger.wrn("Failed to compite offset for ellipse (a=%s and b=%s)", a, b);
					}

					if (offset == 0)
						continue;
					double dH = Math.sin(terrainAngle) * offset;
					double dt = dH / Configuration.c;

					final Double tRNew = simVar.tR + dt;
					final Double tENew = simVar.tE.get(sat) + dt;

					/* already in db ? */
					ObjectSet<SimVars> q = newDb.query(new Predicate<SimVars>() {
						@Override
						public boolean match(SimVars candidate) {
							return (tRNew == candidate.tR);
						}
					});

					SimVars simVarNew;
					if (q.size() > 0)
						simVarNew = q.next();
					else
						simVarNew = new SimVars(simVar);

					simVarNew.apply(simVar, sat);
					simVarNew.tR = tRNew;
					simVarNew.tE.put(sat, tENew);

					newDb.store(simVarNew);
				}
			}
			newDb.commit();

		}

		newDb.commit();
		simulation.setDb(newDb);

		return simulation;
	}
}
