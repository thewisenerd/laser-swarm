package com.google.code.laserswarm.conf;

import org.simpleframework.xml.Element;

public class Satellite {
	@Element
	private float semimajorAxis = 0f;

	@Element
	private float eccentricity = 0f;

	@Element
	private float inclination = 0f;

	@Element
	private float rightAngleOfAscendingNode = 0f;

	@Element
	private float trueAnomaly = 0f;

	@Element
	private float argumentOfPerigee = 0f;
	
	public float getArgumentOfPerigee() {
		return argumentOfPerigee;
	}
	public float getEccentricity() {
		return eccentricity;
	}
	public float getInclination() {
		return inclination;
	}
	public float getRightAngleOfAscendingNode() {
		return rightAngleOfAscendingNode;
	}
	public float getSemimajorAxis() {
		return semimajorAxis;
	}
	public float getTrueAnomaly() {
		return trueAnomaly;
	}
	
	public Satellite() {
		trueAnomaly = semimajorAxis = rightAngleOfAscendingNode = inclination = eccentricity = argumentOfPerigee = 0f;
	}
	
	public Satellite(float a, float e, float i, float raan, float ta, float w) {
		trueAnomaly = ta;
		semimajorAxis = a;
		rightAngleOfAscendingNode = raan;
		inclination = i;
		eccentricity = e;
		argumentOfPerigee = w;
	}
}