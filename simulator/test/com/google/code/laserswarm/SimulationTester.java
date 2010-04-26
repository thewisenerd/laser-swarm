package com.google.code.laserswarm;

import java.lang.reflect.Field;

import junit.framework.TestCase;

import com.google.code.laserswarm.conf.Configuration;

public class SimulationTester extends TestCase {

	public static final String	CfgName	= "unitTestConfig.xml";

	public void testConfiguration() {
		Configuration cfg = new Configuration();
		Configuration.write(CfgName, cfg);
		Configuration.read(CfgName);
		Configuration cfg2 = Configuration.getInstance();

		Field[] f = cfg.getClass().getFields();
		for (Field field : f) {
			try {
				assertEquals(field.get(cfg), field.get(cfg2));
			} catch (IllegalAccessException e) {
				fail("Cannot compare");
			}
		}
	}
}