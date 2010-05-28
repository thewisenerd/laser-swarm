package com.google.code.laserswarm.simulation.postSimulation;

import static com.google.code.laserswarm.math.VectorMath.relative;

import java.util.List;

import javax.vecmath.Vector3d;

import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.math.EllipticalArea;
import com.google.code.laserswarm.simulation.SimVars;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class SlopeSpread implements IPostSimulation {

	private static final Logger	logger	= Logger.get(SlopeSpread.class);

	@Override
	public List<SimVars> modify(List<SimVars> data, Constellation constellation) {
		logger.inf("Post-process: Slope spreading");

		for (SimVars simVars : data) {
			double terrainAngle = simVars.surfNormal.angle(new Vector3d(0, 0, 1));
			double satDistance = relative(simVars.p0, simVars.pR).length();

			double alpha = constellation.getEmitter().getBeamDivergence();
			double beta = Math.PI - alpha - (Math.PI - terrainAngle);
			double a = (Math.sin(alpha) / Math.sin(beta)) * satDistance;
			double b = alpha * satDistance;

			EllipticalArea area = new EllipticalArea();

			relative(new Vector3d(), new Vector3d());
			;
		}

		return data;
	}
}
