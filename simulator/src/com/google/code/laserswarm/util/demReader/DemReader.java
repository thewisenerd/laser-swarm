package com.google.code.laserswarm.util.demReader;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.code.laserswarm.conf.Configuration;
import com.google.code.laserswarm.conf.Configuration.Actions;
import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.earthModel.IElevationModel;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.lyndir.lhunath.lib.system.logging.Logger;

public abstract class DemReader {

	private static final Logger	logger	= Logger.get(DemReader.class);

	public static ImmutableSet<ElevationModel> getDefaultDems() {
		ImmutableSet<ElevationModel> dems;

		File asterGdemBe = new File(Configuration.demDir, "ASTER_GDEM_BE");
		if (asterGdemBe.exists()) {
			dems = parseDem(asterGdemBe.listFiles());
		} else {
			dems = parseDem(Configuration.demDir.listFiles());
		}

		return dems;
	}

	private static ImmutableSet<ElevationModel> parseDem(File[] listFiles) {
		return parseDem(Arrays.asList(listFiles));
	}

	public static ElevationModel parseDem(File demFile) throws DemCreationException {
		String fileName = demFile.getName();

		File cf = new File(Configuration.nonVolitileCache, demFile.getName() + ".cache.tiff");
		File ef = new File(Configuration.nonVolitileCache, demFile.getName() + ".env.xml");
		boolean cached = cf.exists() && ef.exists();
		ElevationModel dem;

		String type = fileName.substring(fileName.lastIndexOf(".") + 1);
		if (type.equals("asc") || type.equals("AGR")) {
			if (Configuration.hasAction(Actions.DEM_CACHE) && cached) {
				logger.inf("Loading %s from cache (%s,%s)", fileName, cf, ef);
				dem = new ElevationModel(cf, ef);
			} else {
				logger.inf("Using ASCII DEM PARSER");
				dem = new ArcInfoASCII_1(demFile).parse();
			}
		} else if (type.equals("tif")) {
			try {
				dem = new GeoTiffParser(demFile).parse();
			} catch (DemCreationException e) {
				if (Configuration.hasAction(Actions.DEM_CACHE) && cached) {
					logger.inf("Loading %s from cache (%s,%s)", fileName, cf, ef);
					dem = new ElevationModel(cf, ef);
				} else
					throw new DemFormatException();
			}
		} else {
			throw new DemFormatException();
		}

		if (!cached && Configuration.hasAction(Actions.DEM_CACHE))
			dem.toCache(cf, ef);

		dem.shrink();
		return dem;
	}

	public static ImmutableSet<ElevationModel> parseDem(List<File> demFiles) {
		final Set<ElevationModel> dems = Sets.newHashSet();

		logger.dbg("Reading dems: %s", demFiles);

		Set<Thread> waiting = Sets.newHashSet();
		for (int i = 0; i < demFiles.size(); i++) {
			final File demFile = demFiles.get(i);
			Thread tr = new Thread(String.format("Parser %s", demFile.getName())) {
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
				logger.inf("Sarting %s (waiting:%s, running: %s)", tr, waiting.size(), running.size());
			}
			do {
				// logger.dbg("%d threads alive", running.size());
				for (Thread thread : ImmutableSet.copyOf(running))
					if (!thread.isAlive()) {
						running.remove(thread);
					}

				try {
					Thread.sleep(100);
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
