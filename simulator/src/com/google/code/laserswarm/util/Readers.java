package com.google.code.laserswarm.util;

import java.io.IOException;

import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;

import com.google.common.base.Splitter;
import com.google.common.io.LineProcessor;

public abstract class Readers {

	public static LineProcessor<Double> lastDoubleReader() {
		return new LineProcessor<Double>() {
			Double	i;

			@Override
			public Double getResult() {
				return i;
			}

			@SuppressWarnings("static-access")
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
	}

	public static LineProcessor<Matrix> matrixRowReader(final int nRows, final int nCols) {
		return new LineProcessor<Matrix>() {
			Matrix	m	= MatrixFactory.dense(nRows, nCols);
			int		row	= 0;

			@Override
			public Matrix getResult() {
				return m;
			}

			@Override
			public boolean processLine(String line) throws IOException {
				Iterable<String> cols = Splitter.on(" ").trimResults().omitEmptyStrings().split(line);
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
	}

}
