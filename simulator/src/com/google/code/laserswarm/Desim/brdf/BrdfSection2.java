package com.google.code.laserswarm.Desim.brdf;

import java.util.Set;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.opengis.coverage.PointOutsideCoverageException;

import com.google.common.collect.Sets;

public class BrdfSection2 {

	private static final double	RES	= 15 * (Math.PI / 180);

	public static BrdfSection2 findSection(Tuple3d vector) {
		double lonTile = (Math.floor(vector.y / RES)) * RES;
		double latTile = (Math.floor(vector.z / RES)) * RES;

		return new BrdfSection2(lonTile, RES, latTile, RES);
	}

	private Envelope2D		bounds;
	private Set<Tuple3d>	vectors	= Sets.newHashSet();

	public BrdfSection2(double lonMin, double dLon, double latMin, double dLat) {
		bounds = new Envelope2D(null, lonMin, latMin, dLon, dLat);
	}

	public void add(Tuple3d vector) {
		if (!bounds.contains(new DirectPosition2D(vector.y, vector.z)))
			throw new PointOutsideCoverageException(vector.toString());
		vectors.add(vector);
	}

	public Envelope2D getBounds() {
		return bounds;
	}

	public Point2d getCenter() {
		return new Point2d(bounds.getCenterX(), bounds.getCenterY());
	}

	public int getPhotonCountAvg() {
		int photons = 0;
		for (Tuple3d vec : vectors)
			photons += vec.x;
		return photons / vectors.size();
	}

	public Point3d getResulatant() {

		return new Point3d(getPhotonCountAvg(), bounds.getCenterX(), bounds.getCenterY());
	}

	@Override
	public int hashCode() {
		return bounds.hashCode();
	}

}
