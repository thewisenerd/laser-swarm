package com.google.code.laserswarm.simulation;

public class SimulationException extends RuntimeException {

	private static final long	serialVersionUID	= 1L;

	public SimulationException() {
		super();
	}

	public SimulationException(Throwable e, String string, Object... args) {
		super(String.format(string, args), e);
	}
}
