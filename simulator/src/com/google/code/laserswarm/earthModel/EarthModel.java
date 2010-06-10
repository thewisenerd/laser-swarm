package com.google.code.laserswarm.earthModel;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.media.jai.Interpolation;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.Interpolator2D;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.operation.projection.PointOutsideEnvelopeException;
import org.opengis.geometry.Envelope;
import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.exceptions.MatrixException;

import com.google.code.laserswarm.util.demReader.DemReader;
import com.google.common.collect.Sets;
import com.lyndir.lhunath.lib.system.logging.Logger;

/**
 * 
 * @author Simon Billemont, TUDelft, Faculty Aerospace Engineering (aodtorusan@gmail.com or
 *         s.billemont@student.tudelft.nl)
 * 
 */
public class EarthModel implements IElevationModel {

	private static EarthModel	earth;

	public static EarthModel getDefaultModel() {
		if (earth == null) {
			earth = new EarthModel(DemReader.getDefaultDems());
			earth.loadCoef();
		}
		return earth;
	}

	private Set<ElevationModel>	dems	= Sets.newHashSet();
	private GridCoverage2D		kappaMinnaertMap;
	private GridCoverage2D		surfaceRefractionMap;

	private GridCoverage2D		thetaHenyeyGreensteinMap;

	private static final Logger	logger	= Logger.get(EarthModel.class);

	public EarthModel() {
		loadCoef(new File("."));
	}

	public EarthModel(ElevationModel dem) {
		this.dems.add(dem);
		loadCoef();
	}

	public EarthModel(ElevationModel dem, File coefFolder) {
		this.dems.add(dem);
		loadCoef(coefFolder);
	}

	public EarthModel(Set<ElevationModel> dems) {
		this.dems = dems;
	}

	public EarthModel(Set<ElevationModel> dems, File coefFolder) {
		this.dems = dems;
		loadCoef();
	}

	public void add(ElevationModel dem) {
		dems.add(dem);
	}

	public ElevationModel findCoverage(DirectPosition2D point) {
		for (ElevationModel dem : dems) {
			if (dem.getCoverage().getEnvelope2D().contains(point))
				return dem;
		}
		return null;
	}

	public Envelope2D getCompleteEnvelope2D() {
		Envelope2D env = null;
		for (ElevationModel dem : dems) {
			Envelope2D e = dem.getCoverage().getEnvelope2D();
			if (env == null) {
				env = e;
				continue;
			}
			env.add(e.getBounds2D());
		}
		return env;
	}

	public Set<ElevationModel> getDem() {
		return dems;
	}

	@Override
	public double getElevation(DirectPosition2D point) throws PointOutsideEnvelopeException {
		ElevationModel dem = findCoverage(point);
		if (dem == null)
			throw new PointOutsideEnvelopeException();
		else
			return dem.getElevation(point);
	}

	public double getElevation(Point3d sphere) throws PointOutsideEnvelopeException {
		DirectPosition2D point = new DirectPosition2D(sphere.y * (180 / Math.PI), sphere.z
				* (180 / Math.PI));
		return getElevation(point);
	}

	@Override
	public Point3d getIntersection(Vector3d direction, Point3d origin)
			throws PointOutsideEnvelopeException {
		for (ElevationModel dem : dems) {
			try {
				return dem.getIntersection(direction, origin);
			} catch (PointOutsideEnvelopeException e) {
			}
		}
		throw new PointOutsideEnvelopeException("The ray does not intersect the coverage.");
	}

	public GridCoverage2D getKappaMinnaertMap() {
		return kappaMinnaertMap;
	}

	public ScatteringParam getScatteringParam(DirectPosition2D point) {
		float[] indexOfRefraction = (float[]) surfaceRefractionMap.evaluate(point);
		float[] kappaMinnaert = (float[]) kappaMinnaertMap.evaluate(point);
		float[] thetaHenyeyGreenstein = (float[]) thetaHenyeyGreensteinMap.evaluate(point);

		return new ScatteringParam(indexOfRefraction[0], kappaMinnaert[0], thetaHenyeyGreenstein[0]);
	}

	@Override
	public Vector3d getSurfaceNormal(DirectPosition2D pos) {
		ElevationModel dem = findCoverage(pos);
		if (dem == null)
			return null;
		else
			return dem.getSurfaceNormal(pos);
	}

	public GridCoverage2D getSurfaceRefractionMap() {
		return surfaceRefractionMap;
	}

	public GridCoverage2D getThetaHenyeyGreensteinMap() {
		return thetaHenyeyGreensteinMap;
	}

	public void loadCoef() {
		loadCoef(new File("."));
	}

	public void loadCoef(File baseFolder) {
		try {
			File kappa = new File(baseFolder, "kappaMinnaertMap.csv");
			File refr = new File(baseFolder, "surfaceRefractionMap.csv");
			File greenSt = new File(baseFolder, "thetaHenyeyGreensteinMap.csv");

			Matrix m_kappa = null;
			Matrix m_refr = null;
			Matrix m_greenSt = null;
			try {
				m_kappa = MatrixFactory.importFromFile(kappa);
				m_refr = MatrixFactory.importFromFile(refr);
				m_greenSt = MatrixFactory.importFromFile(greenSt);
			} catch (MatrixException e1) {
				logger.err(e1, "Cannot import scatter coef");
			} catch (IOException e1) {
				logger.err(e1, "Cannot import scatter coef");
			}

			Envelope e = getCompleteEnvelope2D();
			kappaMinnaertMap = Interpolator2D.create(new GridCoverageFactory().create("KAPPA", // 
					m_kappa.toFloatArray(), e), Interpolation.getInstance(Interpolation.INTERP_BICUBIC));
			surfaceRefractionMap = Interpolator2D.create(new GridCoverageFactory().create("REFR", // 
					m_refr.toFloatArray(), e), Interpolation.getInstance(Interpolation.INTERP_BICUBIC));
			thetaHenyeyGreensteinMap = Interpolator2D.create(new GridCoverageFactory().create("GREENST", //
					m_greenSt.toFloatArray(), e), Interpolation
					.getInstance(Interpolation.INTERP_BICUBIC));
		} catch (Exception e) {
		}
	}
}
