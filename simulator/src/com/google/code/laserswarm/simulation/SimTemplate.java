package com.google.code.laserswarm.simulation;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Constellation;

public class SimTemplate {

	private Configuration	config;
	private Constellation	constellation;

	public SimTemplate(Configuration config, Constellation constellation) {
		this.config = config;
		this.constellation = constellation;
	}

	public Configuration getConfig() {
		return config;
	}

	public Constellation getConstellation() {
		return constellation;
	}

}
