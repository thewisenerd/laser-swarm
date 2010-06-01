package com.google.code.laserswarm.conf;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;

public class Constellation {

	/**
	 * Make a simple constellation with one receiver and emitter, in the same orbit
	 * 
	 * @param power
	 * @param aperature
	 * @param altitude
	 * @return
	 */
	public static Constellation simpleConstellation(double power, double aperature, double altitude) {
		Satellite emittor = new Satellite("Emittor", aperature, (float) (Configuration.R0 / 1000
				+ altitude), 0f, (float) Math.PI / 2, (float) (8.5 * Math.PI / 180), 0f, 0f);

		LinkedList<Satellite> r = Lists.newLinkedList();
		r.add(new Satellite("Receiver #1", emittor));

		Constellation c = new Constellation(power, 5000, emittor, r);
		c.setName(String.format("Constellation"));
		return c;
	}

	public static Constellation swarm(double power, double aperature, double altitude) {
		Satellite emittor = new Satellite("Emittor", aperature, (float) (Configuration.R0 / 1000
				+ altitude), 0f, (float) Math.PI / 2, (float) (8.5 * Math.PI / 180), 0f, 0f);

		double raan = emittor.getRightAngleOfAscendingNode() + (-2.18 / 180 * Math.PI);
		double ta = emittor.getTrueAnomaly() + (-2.18 / 180 * Math.PI);
		double[][] sats = new double[][] { { +raan, 0 },
											{ -raan, 0 },
											{ 0, +ta },
											{ 0, -ta } };

		List<Satellite> receivers = Lists.newLinkedList();
		for (int i = 0; i < sats.length; i++) {
			double[] config = sats[i];
			Satellite sat = new Satellite(
					String.format("Satellite [RAAN:%f TA:%f]", config[0], config[1]),
					emittor);
			sat.setRightAngleOfAscendingNode(config[0]);
			sat.setTrueAnomaly(config[1]);
			receivers.add(sat);
		}

		return new Constellation(power, 5000, emittor, receivers);
	}

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

	public void setReceivers(List<Satellite> receivers) {
		this.receivers = receivers;
	}

	@Override
	public String toString() {
		return name;
	}
}
