package com.google.code.laserswarm.Desim.brdf;

import static com.google.code.laserswarm.math.Convert.toXYZ;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.apache.commons.math.MathException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.analysis.interpolation.MicrosphereInterpolator;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.math.Distribution;
import com.google.code.laserswarm.util.CSVwriter;
import com.lyndir.lhunath.lib.system.logging.Logger;
import com.wolfram.jlink.MathLinkException;
import com.wolfram.jlink.MathLinkFactory;

public class Brdf extends Distribution {

	private MultivariateRealFunction	interpolatedDiscreteBRDF;

	private DiscreteBrdf				discreteBRDF;

	private static final Logger			logger	= Logger.get(Brdf.class);

	public Brdf(DiscreteBrdf discreteBRDF) throws MathException {
		this(discreteBRDF, 1);
	}

	public Brdf(DiscreteBrdf discreteBRDF, double scale) throws MathException {
		this.discreteBRDF = discreteBRDF;
		MicrosphereInterpolator interpolator = new MicrosphereInterpolator();
		double[][] xVals = discreteBRDF.asArrayPoints();
		double[] yVals = discreteBRDF.asArrayValues(scale);

		double[][] xVals2 = new double[xVals.length][3];
		for (int i = 0; i < xVals.length; i++) {
			Point3d xyz = toXYZ(new Point3d(1, xVals[i][0], xVals[i][1]));
			xVals2[i][0] = xyz.x;
			xVals2[i][1] = xyz.y;
			xVals2[i][2] = xyz.z;
		}

		interpolatedDiscreteBRDF = interpolator.interpolate(xVals2, yVals);
	}

	@Override
	public double probability(Vector3d x) {
		// Point3d sphere = Convert.toSphere(x);
		// double lon = sphere.y;
		// double lat = sphere.z;
		//
		// try {
		// return interpolatedDiscreteBRDF.value(new double[] { lon, lat });
		// } catch (MathException e) {
		// throw new RuntimeException(e);
		// }
		x.normalize();
		try {
			return interpolatedDiscreteBRDF.value(new double[] { x.x, x.y, x.z });
		} catch (MathException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Image toImage() throws LinkageError {
		if (ml == null) {

			File kernelFile = Configuration.getInstance().getMathematicaKernel();
			if (kernelFile == null || !kernelFile.exists())
				throw new LinkageError("Cannot find the Mathematica Kernel");

			try {
				String[] mlArgs = { "-linkmode", "launch", "-linkname", kernelFile.getAbsolutePath() };
				ml = MathLinkFactory.createKernelLink(mlArgs);
				ml.discardAnswer();
			} catch (MathLinkException e) {
				logger.wrn(e, "Kernel connection failed");
				throw new LinkageError("An error occurred connecting to the kernel)");
			}
		}

		File csv = new File(Configuration.volatileCache, "scatter-" + Math.random() + ".csv");
		toCSV(csv.getAbsolutePath());
		File csv2 = new File(Configuration.volatileCache, "scatter-dir-" + Math.random() + ".csv");
		CSVwriter csv2writer = new CSVwriter(csv2, "\t");

		for (BrdfSection2 section : discreteBRDF.getSections()) {
			Point3d xyz = toXYZ(section.getResulatant());
			csv2writer.write(xyz.x, xyz.y, xyz.z);
		}

		byte[] gifData = ml
				.evaluateToImage(
						"Clear[\"Global`*\"];"
								+ "data = Import[\"" + csv.getAbsolutePath() + "\", \"Table\"];"
								+ "directions =  Import[\"" + csv2.getAbsolutePath() + "\", \"Table\"];"
								+ "scale = data[[All, 6]];"
								+ "scale = scale /Mean[scale];"
								+ "coordinates = data[[All, 3 ;; 5]];"
								+ "vectors = Insert[directions, {0, 0, 0},"
									+ "Partition[Range[2, Length[directions] + 1], 1]];"
								+ "cloud = ListPointPlot3D[coordinates*scale];"
								+ "vectorPlot = Graphics3D[{Line[vectors]}];"
								+ "Show[cloud, vectorPlot]",
						800, 600, 0, false);
		Image im = Toolkit.getDefaultToolkit().createImage(gifData);
		return im;
	}
}
