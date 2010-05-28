package com.google.code.laserswarm.simulation;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.earthModel.ScatteringCharacteristics;
import com.google.common.io.LineProcessor;
import com.thoughtworks.xstream.XStream;

public class SimVars {

	public static transient LineProcessor<SimVars>	DESERIALIZER;
	static {
		DESERIALIZER = new LineProcessor<SimVars>() {
			private SimVars	last;

			@Override
			public SimVars getResult() {
				return last;
			}

			@Override
			public boolean processLine(String line) throws IOException {
				// XStream xstream = new XStream(new JettisonMappedXmlDriver());
				XStream xstream = new XStream();
				last = (SimVars) xstream.fromXML(line);
				return true;
			}
		};
	}

	/**
	 * Time of origin.
	 */
	public double									t0;
	/**
	 * Time from origin to reflection.
	 */
	public double									tR;
	/**
	 * Time from reflection to endpoint.
	 */
	public HashMap<Satellite, Double>				tE;

	/**
	 * Point of origin.
	 */
	public Point3d									p0;
	/**
	 * Point of reflection.
	 */
	public Point3d									pR;
	/**
	 * Endpoints.
	 */
	public HashMap<Satellite, Point3d>				pE;

	/**
	 * Original power.
	 */
	public double									power0;
	/**
	 * Power reflected.
	 */
	public double									powerR;
	/**
	 * Power reflected after scatter.
	 */
	public HashMap<Satellite, Double>				powerR_SC;
	/**
	 * Power reaching endpoints.
	 */
	public HashMap<Satellite, Double>				powerE;

	/**
	 * Photons per square meter.
	 */
	public HashMap<Satellite, Double>				photonDensity;
	/**
	 * Photons received at the endpoint.
	 */
	public HashMap<Satellite, Integer>				photonsE;
	/**
	 * Scattering characteristics for this footprint.
	 */
	public ScatteringCharacteristics				scatter;

	/**
	 * Is the footprint illuminated by the sun ?
	 */
	public boolean									illuminated;

	/**
	 * If illuminated by the sun, this is the incidence angle of the sun
	 */
	public Vector3d									sunVector;
	/**
	 * Surface normal
	 */
	public Vector3d									surfNormal;

	public void reduce() {
		for (Field field : getClass().getDeclaredFields()) {
			if (field.getType().isPrimitive())
				continue;
			else if (!field.getName().equals("photonsE"))
				try {
					field.set(this, null);
				} catch (IllegalAccessException e) {
				}
		}
	}

	public String serialize() {
		// XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
		XStream xstream = new XStream();
		String str = xstream.toXML(this);
		str = str.replaceAll("\\r", "");
		str = str.replaceAll("\\n", "\\\\n");
		return str;
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("Time:\n");
		s.append(String.format("T0:%s\n", t0));
		s.append(String.format("TR:%s\n", tR));
		for (Satellite sat : tE.keySet())
			s.append(String.format("\tTE(%s):%s\n", sat, tR));

		s.append("Points:\n");
		s.append(String.format("p0:%s\n", p0));
		s.append(String.format("pR:%s\n", pR));
		for (Satellite sat : pE.keySet())
			s.append(String.format("\tpE(%s):%s\n", sat, pE));

		s.append("Power:\n");
		s.append(String.format("power0:%s\n", power0));
		s.append(String.format("powerR:%s\n", powerR));
		for (Satellite sat : pE.keySet()) {
			s.append(String.format("\tpowerR_SC(%s):%s\n", sat, powerR_SC));
			s.append(String.format("\tpowerE(%s):%s\n", sat, powerE));
		}

		for (Satellite sat : pE.keySet()) {
			s.append(String.format("\tphotonDensity(%s):%s\n", sat, photonDensity));
			s.append(String.format("\tphotonsE(%s):%s\n", sat, photonsE));
		}

		return s.toString();
	}
}
