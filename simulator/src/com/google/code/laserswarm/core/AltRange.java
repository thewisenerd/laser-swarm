package com.google.code.laserswarm.core;

import java.util.LinkedList;
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
		Satellite emittor = new Satellite("SAT01", (0.08 * 0.08), 6370f + alt, 0f, (float) Math.PI / 2,
				(float) (8.5 * Math.PI / 180), 0f, 0f);
		LinkedList<Satellite> r = Lists.newLinkedList();
		r.add(emittor);
		Constellation c = new Constellation(30 * (1. / 3), 5000, emittor, r);
		c.setName(String.format("Constellation %s km", alt));
		return c;
	}

	public AltRange() {
		timeSteps = new double[][] { { 0, 4000000 } };
	}

	@Override
	protected List<Constellation> mkConstellations() {
		List<Constellation> constellations = Lists.newLinkedList();
		for (int i = 300; i <= 501; i += 25) {
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
