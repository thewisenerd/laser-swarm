package com.google.code.laserswarm.earthModel;

import java.io.IOException;

import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.enums.FileFormat;
import org.ujmp.core.exceptions.MatrixException;

import com.lyndir.lhunath.lib.system.logging.Logger;

public class ScatteringModelGenerator {
	private static final Logger	logger	= Logger.get(ElevationModel.class);

	public static void main(String[] args) {
		int mHeight = 25;
		int mWidth = 25;
		Matrix indRefr = MatrixFactory.dense(mHeight, mWidth);
		for (int j = 0; j < mHeight; j++) {
			for (int i = 0; i < mWidth; i++) {
				indRefr.setAsDouble(3, j, i);
			}
		}
		try {
			indRefr.exportToFile(FileFormat.CSV,
					"/media/DATA/coding/java/Laser SWARM/simulator/surfaceRefractionMap.csv");
		} catch (MatrixException e) {
			logger.inf(e, "Couldn't write Matrix.");
		} catch (IOException e) {
			logger.inf(e, "Matrix file writing failed.");
		}

	}
}
