package com.google.code.laserswarm.util.demReader;

import java.io.File;
import java.util.List;
import java.util.Set;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.earthModel.IElevationModel;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.lyndir.lhunath.lib.system.logging.Logger;

public abstract class DemReader {

	private static final Logger	logger	= Logger.get(DemReader.class);

	public static void main(String[] args) {
		try {
			DemReader.parseDem(new File("DEM/ASTGTM_N48E000_dem.asc"));
		} catch (DemCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ElevationModel parseDem(File demFile) throws DemCreationException {
		String fileName = demFile.getName();
		String type = fileName.substring(fileName.lastIndexOf(".") + 1);
		if (type.equals("asc") || type.equals("AGR")) {
			logger.inf("Using ASCII DEM PARSER");
			return new ArcInfoASCII_1(demFile).parse();
		} else if (type.equals("tif")) {
			return new GeoTiffParser(demFile).parse();
		} else {
			throw new DemFormatException();
		}
	}

	public static ImmutableSet<ElevationModel> parseDem(List<File> demFiles) {
		final Set<ElevationModel> dems = Sets.newHashSet();

		logger.dbg("Reading dems: %s", demFiles);

		Set<Thread> waiting = Sets.newHashSet();
		for (int i = 0; i < demFiles.size(); i++) {
			final File demFile = demFiles.get(i);
			Thread tr = new Thread(String.format("Parser %s", demFile)) {
				@Override
				public void run() {
					try {
						ElevationModel dem = DemReader.parseDem(demFile);
						dems.add(dem);
					} catch (DemCreationException e) {
						logger.wrn("Cannot parse dem %s", demFile);
					}
				}
			};
			waiting.add(tr);
		}

		Set<Thread> running = Sets.newHashSet();
		while (running.size() > 0 || waiting.size() > 0) {
			if (waiting.iterator().hasNext()) {
				Thread tr = waiting.iterator().next();
				waiting.remove(tr);
				running.add(tr);
				tr.start();
			}
			do {
				// logger.dbg("%d threads alive", running.size());
				for (Thread thread : ImmutableSet.copyOf(running))
					if (!thread.isAlive()) {
						running.remove(thread);
					}

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					logger.wrn("Interrupted while sleeping");
				}
			} while (running.size() >= Configuration.demThreads);
		}
		return ImmutableSet.copyOf(dems);
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
