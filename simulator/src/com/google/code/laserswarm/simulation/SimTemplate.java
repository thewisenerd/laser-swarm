package com.google.code.laserswarm.simulation;

import com.google.code.laserswarm.conf.Constellation;

public class SimTemplate {

	private double			T0		= 0;
	private double			TE		= 500000;
	private long			samples	= 1000L;

	private boolean			useTime	= true;

	private Constellation	constellation;

	public boolean useTime() {
		return useTime;
	}

	public long getSamples() {
		return samples;
	}

	public SimTemplate(Constellation constellation, long samples) {
		this.constellation = constellation;
		useTime = false;
		this.samples = samples;
	}

	public SimTemplate(Constellation constellation, double T0, double TE) {
		this.constellation = constellation;
		useTime = true;
		setTime(T0, TE);
	}

	public SimTemplate(Constellation constellation) {
		this(constellation, 1000L);
	}

	public Constellation getConstellation() {
		return constellation;
	}

	public double getT0() {
		return T0;
	}

	public double getTE() {
		return TE;
	}

	public void setTime(double T0, double TE) {
		useTime = true;
		this.T0 = T0;
		this.TE = TE;
	}

	@Override
	public String toString() {
		return "Template-" + constellation.toString();
	}

}
