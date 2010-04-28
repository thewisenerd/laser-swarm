package com.google.code.laserswarm.earthModel;

import java.io.File;
import java.io.IOException;

import javax.media.jai.Interpolation;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.Interpolator2D;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.geometry.Envelope;
import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.exceptions.MatrixException;

import com.lyndir.lhunath.lib.system.logging.Logger;

/**
 * 
 * @author Simon Billemont, TUDelft, Faculty Aerospace Engineering (aodtorusan@gmail.com or
 *         s.billemont@student.tudelft.nl)
 * 
 */
public class EarthModel {

	private ElevationModel		dem;
	private GridCoverage2D		kappaMinnaertMap;
	private GridCoverage2D		surfaceRefractionMap;
	private GridCoverage2D		thetaHenyeyGreensteinMap;

	private static final Logger	logger	= Logger.get(EarthModel.class);

	public EarthModel(ElevationModel dem) {
		this.dem = dem;
		loadCoef(new File("."));
	}

	public EarthModel(ElevationModel dem, File coefFolder) {
		this.dem = dem;
		loadCoef(coefFolder);
	}

	public ElevationModel getDem() {
		return dem;
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

	public GridCoverage2D getSurfaceRefractionMap() {
		return surfaceRefractionMap;
	}

	public GridCoverage2D getThetaHenyeyGreensteinMap() {
		return thetaHenyeyGreensteinMap;
	}

	private void loadCoef(File baseFolder) {
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

		Envelope e = dem.getCoverage().getEnvelope();
		kappaMinnaertMap = Interpolator2D.create(new GridCoverageFactory().create("KAPPA", // 
				m_kappa.toFloatArray(), e), Interpolation.getInstance(Interpolation.INTERP_BICUBIC));
		surfaceRefractionMap = Interpolator2D.create(new GridCoverageFactory().create("REFR", // 
				m_refr.toFloatArray(), e), Interpolation.getInstance(Interpolation.INTERP_BICUBIC));
		thetaHenyeyGreensteinMap = Interpolator2D.create(new GridCoverageFactory().create("GREENST", //
				m_greenSt.toFloatArray(), e), Interpolation.getInstance(Interpolation.INTERP_BICUBIC));
	}
}
