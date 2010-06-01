package com.google.code.laserswarm.core;

import java.util.List;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.conf.Configuration.Actions;
import com.google.code.laserswarm.simulation.SimTemplate;
import com.google.code.laserswarm.simulation.SimVars;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class AltRange extends LaserSwarm {

	private static final Logger	logger	= Logger.get(AltRange.class);

	public static void main(String[] args) {
		Configuration.getInstance();
		Configuration.setMode(Sets.newHashSet( //
				Actions.SIMULATE, Actions.PROSPECT, Actions.COUNT_ONLY, Actions.FORCE_FLAT));
		AltRange sim = new AltRange();
		sim.run();
	}

	private static Constellation mkConstellation(float alt) {
		return Constellation.swarm(5, 0.007, alt);
	}

	public AltRange() {
		timeSteps = new double[][] { { 0, 4000000 } };
	}

	@Override
	protected List<Constellation> mkConstellations() {
		List<Constellation> constellations = Lists.newLinkedList();
		for (int i = 300; i <= 376; i += 25) {
			constellations.add(mkConstellation(i));
		}
		return constellations;
	}

	@Override
	protected void simulate() {
		super.simulate();
		for (SimTemplate tmpl : simulations.keySet()) {
			double nrP = 0;
			long samples = 0;
			for (Satellite sat : tmpl.getConstellation().getReceivers()) {
				samples += simulations.get(tmpl).getDataPoints().size();
				for (SimVars var : simulations.get(tmpl).getDataPoints()) {
					nrP += var.photonsE.get(sat);
					// logger.dbg("p=%s", var.photonsE.get(sat));
				}
			}
			logger.inf(tmpl + " nr photons = " + nrP + " of " + samples + " samples\t=> avg: " + nrP
					/ samples);
		}
	}

}
