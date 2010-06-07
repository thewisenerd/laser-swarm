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
import com.lyndir.lhunath.lib.system.logging.Logger;

public class SimpleSimulation extends LaserSwarm {

	private static final Logger	logger	= Logger.get(SimpleSimulation.class);

	public static void main(String[] args) {
		Configuration.getInstance().getMode().remove(Actions.DEM_CACHE);
		Configuration.getInstance().getMode().remove(Actions.PLOT_DISK);
		new SimpleSimulation().run();
	}

	@Override
	protected void end() {
		for (SimTemplate tmpl : simulations.keySet()) {
			double nrP = 0;
			long samples = 0;
			for (Satellite sat : tmpl.getConstellation().getReceivers()) {
				samples += simulations.get(tmpl).getDataPoints().size();
				int satP = 0;
				for (SimVars var : simulations.get(tmpl).getDataPoints()) {
					satP += var.photonsE.get(sat);
				}
				nrP += satP;
				logger.dbg("Sat=%S\tp=%s (%s)", sat, satP,
						new Double(satP) / simulations.get(tmpl).getDataPoints().size());
			}
			logger.inf(tmpl + " nr photons = " + nrP + " of " + samples + " samples\t=> avg: " + nrP
					/ samples);
		}
	}

	@Override
	protected List<Constellation> mkConstellations() {
		LinkedList<Constellation> c = Lists.newLinkedList();

		Constellation constellation = Constellation.swarm(6, 0.0056, 500);
		c.add(constellation);

		return c;
	}

	@Override
	protected List<SimTemplate> mkTemplates(Constellation constellation) {
		LinkedList<SimTemplate> tmpls = Lists.newLinkedList();
		tmpls.add(new SimTemplate(constellation, 20000));
		return tmpls;
	}

}
