package com.google.code.laserswarm.conf;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import com.google.common.collect.Lists;

public class Constellation {
	@Attribute
	private float power = 0f;
	
	@Element
	private Satellite emitter;
	
	@ElementList
	private List<Satellite> receivers = Lists.newLinkedList();
	
	public Satellite getEmitter() {
		return emitter;
	}
	public float getPower() {
		return power;
	}
	public List<Satellite> getReceivers() {
		return receivers;
	}
	
	public Constellation() {
		power = 0f;
		emitter = new Satellite();
		receivers.add(new Satellite());
		receivers.add(new Satellite()); 
	}
	
	public Constellation(float P, Satellite emit, List<Satellite> recv) {
		power = 0f;
		emitter = emit;
		receivers = recv; 
	}
}
