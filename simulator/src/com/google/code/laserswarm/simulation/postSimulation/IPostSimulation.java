package com.google.code.laserswarm.simulation.postSimulation;

import java.util.List;

import com.google.code.laserswarm.simulation.SimVars;

public interface IPostSimulation {

	public List<SimVars> modify(List<SimVars> data);

}
