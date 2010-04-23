package com.google.code.laserswarm.conf;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import com.google.common.collect.Lists;

public class Constellation {
	@Attribute
	private float			power			= 0f;
	@Element
	private int				pulseFrequency	= 5000;

	@Element
	private Satellite		emitter;

	@ElementList
	private List<Satellite>	receivers		= Lists.newLinkedList();

	public Constellation() {
		power = 0f;
		emitter = new Satellite();
		receivers.add(new Satellite());
		receivers.add(new Satellite());

	}

	public Constellation(float P, int pulseFrequency, Satellite emit, List<Satellite> recv) {
		power = P;
		emitter = emit;
		receivers = recv;
		this.pulseFrequency = pulseFrequency;
	}

	public Satellite getEmitter() {
		return emitter;
	}

	public float getPower() {
		return power;
	}

	public int getPulseFrequency() {
		return pulseFrequency;
	}

	public List<Satellite> getReceivers() {
		return receivers;
	}
}
