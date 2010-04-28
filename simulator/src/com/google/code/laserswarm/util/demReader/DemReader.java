package com.google.code.laserswarm.util.demReader;

import java.io.File;

import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.earthModel.IElevationModel;
import com.lyndir.lhunath.lib.system.logging.Logger;

public abstract class DemReader {

	private static final Logger	logger	= Logger.get(DemReader.class);

	public static void main(String[] args) {
		IElevationModel dem = null;
		try {
			dem = DemReader.parseDem(new File("DEM/ASTGTM_N48E000_dem.asc"));
		} catch (DemCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ElevationModel parseDem(File demFile) throws DemCreationException {
		String fileName = demFile.getName();
		String type = fileName.substring(fileName.lastIndexOf(".") + 1);
		if (type.equals("asc")) {
			logger.inf("Using ASCII DEM PARSER");
			return new ArcInfoASCII_1(demFile).parse();
		} else if (type.equals("tif")) {
			return new GeoTiffParser(demFile).parse();
		} else {
			throw new DemFormatException();
		}
	}

	private File	demFile;

	public DemReader(File demFile) {
		this.demFile = demFile;
	}

	public File getDemFile() {
		return demFile;
	}

	public abstract IElevationModel parse() throws DemCreationException;
}
