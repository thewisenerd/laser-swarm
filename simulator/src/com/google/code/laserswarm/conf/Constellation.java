package com.google.code.laserswarm.conf;

import java.util.Set;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import com.google.common.collect.Sets;

public class Constellation {
	@Attribute
	private float			power			= 0f;
	@Element
	private double			pulseFrequency	= 5000;
	@Element
	private double			laserWaveLength	= 300E-9;

	@Element
	private Satellite		emitter;

	@ElementList
	private Set<Satellite>	receivers		= Sets.newHashSet();

	public Constellation() {
		power = 0f;
		emitter = new Satellite();
		receivers.add(new Satellite());
		receivers.add(new Satellite());
	}

	public Constellation(float P, int pulseFrequency, Satellite emit, Set<Satellite> recv) {
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

	public float getPower() {
		return power;
	}

	public double getPulseFrequency() {
		return pulseFrequency;
	}

	public Set<Satellite> getReceivers() {
		return receivers;
	}
}
