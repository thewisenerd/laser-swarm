package com.google.code.laserswarm.conf;

import java.util.List;

import com.google.common.collect.Lists;

public class Constellation {

	/**
	 * Power that the emmitor has [W] (of the laser beam)
	 */
	private double			power				= 0f;

	/**
	 * Frequency of the laser pulses that are send out
	 */
	private double			pulseFrequency		= 5000;
	/**
	 * Pulse length time [s]
	 */
	private double			pulselength			= 1E-9;
	/**
	 * Laser wave length [m]
	 */
	private double			laserWaveLength		= 500E-9;
	/**
	 * Receiver sensitivity wavelength band [m]
	 */
	private double			receiverBandWidth	= 1E-9;
	/**
	 * Efficiency of the receiver [-]
	 * <p>
	 * (Receiver chip efficency) x (optical filter efficency)
	 * </p>
	 */
	private double			receiverEfficiency	= 0.4 * .9;
	private Satellite		emitter;

	private String			name				= "Constellation";
	private List<Satellite>	receivers			= Lists.newLinkedList();

	public Constellation() {
		power = 0f;
		emitter = new Satellite();
		receivers.add(new Satellite());
		receivers.add(new Satellite());
	}

	public Constellation(double P, double pulseFrequency, Satellite emit, List<Satellite> recv) {
		power = P;
		emitter = emit;
		receivers = recv;
		this.pulseFrequency = pulseFrequency;
	}

	public Satellite getEmitter() {
		return emitter;
	}

	public double getLaserWaveLength() {
		return laserWaveLength;
	}

	public String getName() {
		return name;
	}

	public double getPower() {
		return power;
	}

	public double getPulseFrequency() {
		return pulseFrequency;
	}

	public double getPulselength() {
		return pulselength;
	}

	public double getReceiverBandWidth() {
		return receiverBandWidth;
	}

	public double getReceiverEfficiency() {
		return receiverEfficiency;
	}

	public List<Satellite> getReceivers() {
		return receivers;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
