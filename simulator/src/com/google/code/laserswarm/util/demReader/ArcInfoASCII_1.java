package com.google.code.laserswarm.util.demReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.calculation.Calculation.Ret;

import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.util.DebugUtil;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class ArcInfoASCII_1 extends DemReader {

	private static final Logger	logger	= Logger.get(ArcInfoASCII_1.class);

	public ArcInfoASCII_1(File demFile) {
		super(demFile);
	}

	@Override
	public ElevationModel parse() throws DemCreationException {
		String fileName = getDemFile().getName();
		File projectionFile = new File(getDemFile().getParentFile(), // 
				fileName.substring(0, fileName.lastIndexOf(".")) + "prj");
		/* Use projection file ? */
		/* Load the elevation matrix */

		LineProcessor<Double> lastDouble = new LineProcessor<Double>() {
			Double	i;

			@Override
			public Double getResult() {
				return i;
			}

			@Override
			public boolean processLine(String arg0) throws IOException {
				Iterable<String> res = Splitter.on("\t").on(" ").trimResults().omitEmptyStrings() //
						.split(arg0);
				for (String string : res) {
					try {
						i = Double.parseDouble(string);
					} catch (NumberFormatException e) {
					}
				}
				return true;
			}
		};

		Envelope2D envelope = null;
		Matrix matrix = null;
		Double NODATA_value = -9999d;
		try {
			BufferedReader reader = Files.newReader(getDemFile(), Charsets.UTF_8);
			String line;
			lastDouble.processLine(reader.readLine());
			final int nCols = lastDouble.getResult().intValue();
			lastDouble.processLine(reader.readLine());
			final int nRows = lastDouble.getResult().intValue();
			lastDouble.processLine(reader.readLine());
			double xllcorner = lastDouble.getResult();
			lastDouble.processLine(reader.readLine());
			double yllcorner = lastDouble.getResult();
			lastDouble.processLine(reader.readLine());
			double cellsize = lastDouble.getResult();
			lastDouble.processLine(reader.readLine());
			NODATA_value = lastDouble.getResult();

			try {
				envelope = new Envelope2D(CRS.decode("EPSG:3785"), xllcorner, yllcorner, // 
						nCols * cellsize, nRows * cellsize);
			} catch (FactoryException e) {
				throw new DemCreationException(e);
			}

			logger.inf("Memory usage:\nfreeMemory %s\nmaxMemory %s \ntotalMemory %s", Runtime
					.getRuntime().freeMemory(), //
					Runtime.getRuntime().maxMemory(),// 
					Runtime.getRuntime().totalMemory());

			LineProcessor<Matrix> matrixRowParser = new LineProcessor<Matrix>() {
				Matrix	m	= MatrixFactory.dense(nRows, nCols);
				int		row	= 0;

				@Override
				public Matrix getResult() {
					return m;
				}

				@Override
				public boolean processLine(String line) throws IOException {
					Iterable<String> cols = Splitter.on(" ").trimResults().omitEmptyStrings()
							.split(line);
					int col = 0;
					for (String string : cols) {
						double val = Double.parseDouble(string);
						m.setAsDouble(val, row, col);
						col++;
					}
					row++;
					return false;
				}
			};

			for (int lineNr = 1; lineNr < nRows; lineNr++) {
				matrixRowParser.processLine(reader.readLine());
			}

			matrix = matrixRowParser.getResult();
		} catch (IOException e) {
			logger.err(e, "IOExecption while parsing the DEM using ASCII parser");
		}

		DebugUtil.showMemUsage();

		matrix.replace(Ret.ORIG, NODATA_value, -10);

		GridCoverage2D coverage = new GridCoverageFactory().create("DEM", matrix.toFloatArray(),
				envelope);
		return new ElevationModel(matrix, coverage);

	}
}
