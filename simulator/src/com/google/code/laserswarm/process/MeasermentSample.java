package com.google.code.laserswarm.process;

public class MeasermentSample {

	private double	time;
	private int		photons;

	public MeasermentSample(double time, int photons) {
		this.time = time;
		this.photons = photons;
	}

	public int getPhotons() {
		return photons;
	}

	public double getTime() {
		return time;
	}

	@Override
	public String toString() {
		return String.format("t=%s\tphotons=%s", time, photons);
	}
}
