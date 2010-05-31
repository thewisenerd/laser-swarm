package com.google.code.laserswarm.simulation.postSimulation;

import com.google.code.laserswarm.conf.Constellation;
import com.google.code.laserswarm.simulation.Simulator;

public interface IPostSimulation {

	public Simulator modify(Simulator simulation, Constellation c);

}
