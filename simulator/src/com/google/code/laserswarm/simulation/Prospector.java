package com.google.code.laserswarm.simulation;

import java.util.HashMap;
import java.util.LinkedList;

import javax.vecmath.Point3d;

import org.geotools.geometry.DirectPosition2D;

import com.google.code.laserswarm.Orbit.OrbitClass;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.math.Convert;
import com.google.common.collect.Lists;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class Prospector {

	public interface ProspectorFactory {
		public Prospector getProspector(OrbitClass emittorOrbit,
				HashMap<Satellite, OrbitClass> receiverOrbits,
				EarthModel earth, long samples, double dt);
	}

	public static int					after			= 10;
	public static double				roughTimeStep	= 5;
	private static ProspectorFactory	factory;

	public static ProspectorFactory getFactory() {
		if (factory == null)
			return new ProspectorFactory() {
				@Override
				public Prospector getProspector(OrbitClass emittorOrbit,
						HashMap<Satellite, OrbitClass> receiverOrbits, EarthModel earth, long samples,
						double dt) {
					return new Prospector(emittorOrbit, receiverOrbits, earth, samples, dt);
				}
			};
		else
			return factory;
	}

	public static void setFactory(ProspectorFactory factory) {
		Prospector.factory = factory;
	}

	private LinkedList<Long>				lastNulls	= Lists.newLinkedList();

	private static final Logger				logger		= Logger.get(Prospector.class);
	private OrbitClass						emittorOrbit;
	private HashMap<Satellite, OrbitClass>	receiverOrbits;
	private EarthModel						earth;
	private long							samples;

	private double							dt;

	public Prospector(OrbitClass emittorOrbit, HashMap<Satellite, OrbitClass> receiverOrbits,
			EarthModel earth, long samples, double dt) {
		super();
		this.emittorOrbit = emittorOrbit;
		this.receiverOrbits = receiverOrbits;
		this.earth = earth;
		this.samples = samples;
		this.dt = dt;
	}

	public EarthModel getEarth() {
		return earth;
	}

	public long prospect(long i) {
		if (lastNulls.size() > 0)
			if (i != lastNulls.getLast() + 1)
				lastNulls.clear();

		lastNulls.add(i);

		if (lastNulls.size() <= after) {
			return 1;
		} else {
			lastNulls.clear();
			logger.dbg("In pospector");

			long s = (long) (roughTimeStep / dt);
			boolean overGround = false;
			long moved = 1;

			int j = 0;
			while (!overGround && i + moved < samples) {
				if (j % 10000 == 0)
					logger.dbg("Skipped %s samples already", moved);
				/* Find the current position */
				Point3d point = emittorOrbit.ECEF_point();
				Point3d sphere = Convert.toSphere(point);

				overGround = testPoint(sphere);

				/* propagate */
				emittorOrbit.propogate(s * dt);
				for (OrbitClass orbit : receiverOrbits.values())
					orbit.propogate(s * dt);
				moved += s;
				j++;
			}

			logger.dbg("Prospector found a timestep of roughly %s s or %s samples", //
					moved * dt, moved);

			return moved;
		}
	}

	public void setSamples(long samples) {
		this.samples = samples;
	}

	protected boolean testPoint(Point3d sphere) {
		double lon = sphere.y * (180. / Math.PI);
		double lat = sphere.z * (180. / Math.PI);

		/* Quick test over land */
		DirectPosition2D p2d = new DirectPosition2D(lon, lat);
		ElevationModel dem = earth.findCoverage(p2d);
		if (dem != null)
			return true;
		else
			return false;
	}

}
