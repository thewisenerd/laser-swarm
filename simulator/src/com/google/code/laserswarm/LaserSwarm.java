package com.google.code.laserswarm;

import com.google.code.laserswarm.conf.Configuration;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class LaserSwarm {
	private static final Logger	logger	= Logger.get(LaserSwarm.class);
	public static void main(String[] args) {
		Configuration conf = Configuration.getInstance();
		logger.inf(conf.getFileNameDigitalElevationModel());
	}
}
