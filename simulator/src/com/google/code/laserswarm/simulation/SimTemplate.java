package com.google.code.laserswarm.simulation;

import com.google.code.laserswarm.conf.Constellation;

public class SimTemplate {

	private double			T0	= 579267.5;
	private double			TE	= 579341.4;

	private Constellation	constellation;

	public SimTemplate(Constellation constellation) {
		this.constellation = constellation;
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
		this.T0 = T0;
		this.TE = TE;
	}

	@Override
	public String toString() {
		return "Template - " + constellation.toString();
	}

}
