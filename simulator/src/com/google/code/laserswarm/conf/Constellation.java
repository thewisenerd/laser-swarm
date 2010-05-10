package com.google.code.laserswarm.conf;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import com.google.common.collect.Lists;

public class Constellation {
	@Attribute
	private double			power				= 0f;
	@Element
	private double			pulseFrequency		= 5000;
	@Element
	private double			pulselength			= 3E-9;					// s
	@Element
	private double			laserWaveLength		= 300E-9;
	@Element
	private double			receiverBandWidth	= 1E-9;
	@Element
	private double			receiverEfficiency	= 0.4;

	@Element
	private Satellite		emitter;

	@ElementList
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
}
