package com.google.code.laserswarm;

import jat.cm.KeplerElements;
import jat.cm.TwoBody;

import com.lyndir.lhunath.lib.system.logging.Logger;

public class JATtest {

	private static final Logger	logger	= Logger.get(JATtest.class);

	public static void main(String[] args) {
		TwoBody tb = new TwoBody(new KeplerElements(500, 0.1, 0, 0, 0, 0));
		logger.inf("Sat position:\n %s", tb.PQW2ECI());
	}

}
