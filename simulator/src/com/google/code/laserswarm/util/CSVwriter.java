package com.google.code.laserswarm.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import com.google.common.io.Files;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class CSVwriter {

	private static final Logger	logger	= Logger.get(CSVwriter.class);
	private File				csvFile;

	public CSVwriter(File file) {
		this.csvFile = file;
		try {
			csvFile.delete();
			csvFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void write(List<Object[]> values) {
		for (Object[] value : values)
			write(value);
	}

	public void write(Object... values) {
		write("", "", values);
	}

	public void write(String prefix, String sufix, List<Object[]> values) {
		for (Object[] value : values)
			write(prefix, sufix, value);
	}

	public void write(String prefix, String sufix, Object... values) {
		try {
			StringBuffer str = new StringBuffer();
			for (int i = 0; i < values.length; i++) {
				str.append(prefix);
				str.append(values[i]);
				str.append(sufix);
				if (i + 1 < values.length)
					str.append(",\t");
			}
			Files.append(str + "\n", csvFile, Charset.defaultCharset());
		} catch (IOException e) {
			logger.wrn(e, "Could not write to preflog");
		}
	}

}
