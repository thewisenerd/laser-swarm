package com.google.code.laserswarm.simulation;

import java.util.HashMap;
import java.util.Set;

import javax.vecmath.Point3d;

import org.geotools.geometry.DirectPosition2D;

import com.google.code.laserswarm.Orbit.OrbitClass;
import com.google.code.laserswarm.conf.Satellite;
import com.google.code.laserswarm.earthModel.EarthModel;
import com.google.code.laserswarm.earthModel.ElevationModel;

public class LandProspector extends Prospector {

	private Set<String>	validFiles;

	public LandProspector(Set<String> validFiles, OrbitClass emittorOrbit,
			HashMap<Satellite, OrbitClass> receiverOrbits,
			EarthModel earth, long samples, double dt) {
		super(emittorOrbit, receiverOrbits, earth, samples, dt);
		this.validFiles = validFiles;
	}

	@Override
	protected boolean testPoint(Point3d sphere) {
		if (super.testPoint(sphere)) {
			double lon = sphere.y * (180. / Math.PI);
			double lat = sphere.z * (180. / Math.PI);

			/* Quick test over land */
			DirectPosition2D p2d = new DirectPosition2D(lon, lat);
			ElevationModel dem = getEarth().findCoverage(p2d);

			if (validFiles.contains(dem.getDemFile().getName()))
				return true;
		}
		return false;
	}
}
