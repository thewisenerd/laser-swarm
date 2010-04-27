package com.google.code.laserswarm.conf;

import jat.cm.KeplerElements;

import org.simpleframework.xml.Element;

public class Satellite {
	@Element
	private float	semimajorAxis				= 0f;

	@Element
	private float	eccentricity				= 0f;

	@Element
	private float	inclination					= 0f;

	@Element
	private float	rightAngleOfAscendingNode	= 0f;

	@Element
	private float	trueAnomaly					= 0f;

	@Element
	private float	argumentOfPerigee			= 0f;

	private String	name;

	public Satellite() {
		trueAnomaly = semimajorAxis = rightAngleOfAscendingNode = inclination = eccentricity = argumentOfPerigee = 0f;
	}

	public Satellite(String name, float a, float e, float i, float raan, float ta, float w) {
		this.name = name;
		trueAnomaly = ta;
		semimajorAxis = a;
		rightAngleOfAscendingNode = raan;
		inclination = i;
		eccentricity = e;
		argumentOfPerigee = w;
	}

	public float getArgumentOfPerigee() {
		return argumentOfPerigee;
	}

	public float getEccentricity() {
		return eccentricity;
	}

	public float getInclination() {
		return inclination;
	}

	public KeplerElements getKeplerElements() {
		return new KeplerElements(semimajorAxis, eccentricity, inclination, rightAngleOfAscendingNode,
				argumentOfPerigee, trueAnomaly);
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

	@Override
	public String toString() {
		return name;
	}
}
