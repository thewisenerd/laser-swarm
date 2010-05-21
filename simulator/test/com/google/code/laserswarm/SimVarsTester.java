package com.google.code.laserswarm;

import java.io.IOException;

import junit.framework.TestCase;

import com.google.code.laserswarm.simulation.SimVars;

public class SimVarsTester extends TestCase {

	public static void main(String[] args) {
		new SimVarsTester().testSerialize();
	}

	public static void testSerialize()  {
		SimVars vars = new SimVars();
		String line = vars.serialize();
		try {
			SimVars.DESERIALIZER.processLine(line);
		} catch (IOException e) {
			e.printStackTrace();
		}
		SimVars v = SimVars.DESERIALIZER.getResult();
		System.out.println(v);
	}

}
