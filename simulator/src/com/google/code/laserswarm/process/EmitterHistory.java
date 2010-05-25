package com.google.code.laserswarm.process;

import java.util.List;
import java.util.TreeSet;

import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.math.LookupTable;
import com.google.code.laserswarm.simulation.SimVars;
import com.google.common.collect.Sets;

public class EmitterHistory {

	private Constellation	constellation;
	private double			halfPulseTime;
	public TreeSet<Double>	time	= Sets.newTreeSet();
	private LookupTable		position;

	public EmitterHistory(Constellation constellation, List<SimVars> dataSet) {
		this.constellation = constellation;
		halfPulseTime = (1 / constellation.getPulseFrequency()) / 2;
		position = new LookupTable();

		for (SimVars simVars : dataSet) {
			position.put(simVars.t0, simVars.p0);
			time.add(simVars.t0);
		}

	}

	public Satellite getEm() {
		return constellation.getEmitter();
	}

	public LookupTable getPosition() {
		return position;
	}

	public double getPulseBeforePulse(double t) {
		t = t - halfPulseTime;
		return time.floor(t);
		
	}

	public double getPulseClosesTo(double t) {
		return time.floor(t);
	}
}
