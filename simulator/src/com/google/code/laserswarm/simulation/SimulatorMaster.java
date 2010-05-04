package com.google.code.laserswarm.simulation;

import java.util.HashMap;
import java.util.LinkedHashSet;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lyndir.lhunath.lib.system.logging.Logger;

/**
 * 
 * @author Simon Billemont, TUDelft, Faculty Aerospace Engineering (aodtorusan@gmail.com or
 *         s.billemont@student.tudelft.nl)
 * 
 */
public class SimulatorMaster {

	private EarthModel					earth;
	private LinkedHashSet<SimTemplate>	templates	= Sets.newLinkedHashSet();

	private static final Logger			logger		= Logger.get(SimulatorMaster.class);

	public SimulatorMaster(EarthModel earth) {
		this.earth = earth;
	}

	public void addSimTemplate(SimTemplate templ) {
		templates.add(templ);
	}

	/**
	 * Run all the added templates.
	 */
	public HashMap<SimTemplate, Simulator> runSim() {
		HashMap<SimTemplate, Simulator> runningTemplates = Maps.newHashMap();
		HashMap<SimTemplate, Simulator> doneTemplates = Maps.newHashMap();
		while (templates.size() > 0 || runningTemplates.size() > 0) {
			/* Run the next pending template */
			if (templates.size() > 0) {
				SimTemplate templ = templates.iterator().next();
				Simulator sim = new Simulator(templ, earth);
				sim.start();
				templates.remove(templ);
				runningTemplates.put(templ, sim);
			}

			/* Remove any template that is done simulating */
			ImmutableSet<SimTemplate> runningTemplatesCopy = ImmutableSet.copyOf(runningTemplates
					.keySet());
			for (SimTemplate templ : runningTemplatesCopy) {
				Simulator sim = runningTemplates.get(templ);
				if (!sim.getThread().isAlive()) {
					runningTemplates.remove(templ);
					doneTemplates.put(templ, sim);
				}
			}

			/* Make sure we don't have more then the max amount of threads running */
			do {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					logger.err(e, "INTERRUPTED while simulating");
				}
			} while (runningTemplates.size() >= Configuration.simThreads);

		}
		return doneTemplates;
	}
}
