package com.google.code.laserswarm.util.demReader;

import java.io.File;

import com.google.code.laserswarm.earthModel.ElevationModel;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class DemParseThread extends Thread {
	private static final Logger	logger	= Logger.get(DemParseThread.class);

	private File				demFile;
	private ElevationModel		dem;

	public DemParseThread(String theadName, File demFile) {
		super(theadName);
		this.demFile = demFile;
	}

	public ElevationModel getDem() {
		return dem;
	}

	@Override
	public void run() {
		try {
			dem = DemReader.parseDem(demFile);
		} catch (DemCreationException e) {
			logger.wrn("Cannot parse dem %s", demFile);
		}
	}
}