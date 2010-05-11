package com.google.code.laserswarm.conf;

import jat.spacetime.CalDate;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lyndir.lhunath.lib.system.logging.Logger;

@Root
public class Configuration {

	public enum Actions {
		SIMULATE, PROCESS, TABULATE, PLOT_DISK, PLOT_SCREEN, SLEEP;
	}

	private static Configuration	instance;

	private static final String		configName						= "configuration.xml";

	/**
	 * Location of the DEMs
	 */
	public static File				demDir							= new File("DEM");
	/**
	 * Earth radius based on EPSG:3785 ellipsoid (spheroid)
	 */
	public static double			R0								= 6378137;
	/**
	 * Orbit epoch date
	 */
	public static final CalDate		epoch							= new CalDate(2000, 7, 1, 0, 0, 0);
	/**
	 * Stefan-Boltzmann constant
	 */
	public static double			sigma							= 5.67E-8;
	/**
	 * Planck's constant
	 */
	public static double			h								= 6.626068E-34;
	/**
	 * Light speed
	 */
	public static double			c								= 299792458;
	/**
	 * Boltzmann constant
	 */
	public static double			k								= 1.3806503E-23;
	/**
	 * Sun's grey body temperature
	 */
	public static double			TSun							= 5800;
	/**
	 * Sun's grey body emissivity
	 */
	public static double			epsSun							= 0.99;
	/**
	 * Earth's grey body temperature
	 */
	public static double			TEarth							= 254.356;
	/**
	 * Earth's grey body emissivity
	 */
	public static double			epsEarth						= 1;

	@Attribute
	private static Set<Actions>		mode;
	static {
		mode = Sets.newHashSet();
		mode.add(Actions.SIMULATE);
		mode.add(Actions.PROCESS);
		mode.add(Actions.PLOT_DISK);
		mode.add(Actions.TABULATE);
	}

	public static int				simThreads						= 4;
	public static int				demThreads						= 3;

	@Attribute
	private float					atmOpticalThickness				= 0.25f;

	@Element
	private String					filePrefixReport				= " ";

	@Element
	private String					filePathReport					= " ";

	@Element
	private String					fileNameDigitalElevationModel	= " ";

	@Element
	private String					fileNameScatterModel			= " ";

	@Deprecated
	@ElementList
	private List<Constellation>		constellations					= Lists.newLinkedList();

	private static final Logger		logger							= Logger.get(Configuration.class);

	public static Configuration getInstance() {
		if (instance == null) {
			boolean success = Configuration.read(configName);
			if (!success) {
				instance = new Configuration();
			}
		}
		return instance;
	}

	/**
	 * Make a new config file
	 */
	public static void main(String[] args) {
		getInstance();
		String name = Configuration.configName;
		logger.inf("Removing %s", name);
		try {
			(new File(name)).delete();
		} catch (Exception e) {
		}
		logger.inf("Making a new cfg and saving");
		write(name);
	}

	public static boolean read(String filename) {
		boolean success = true;
		Serializer serializer = new Persister();
		File source = new File(filename);
		try {
			instance = serializer.read(Configuration.class, source);
		} catch (Exception e) {
			success = false;
			logger.inf(e, "Configuration file reading failed(%s)", configName);
		}
		return success;
	}

	public static boolean write() {
		return write(configName);
	}

	public static boolean write(String filename) {
		return write(filename, getInstance());
	}

	public static boolean write(String filename, Configuration cfg) {
		boolean success = true;
		Serializer serializer = new Persister();
		File result = new File(filename);
		try {
			serializer.write(cfg, result);
			logger.inf("Configuration file written (%s)", filename);
		} catch (Exception e) {
			success = false;
			logger.inf(e, "Configuration file writing failed (%s)", filename);
		}
		return success;
	}

	public Configuration() {
		constellations.add(new Constellation());
		constellations.add(new Constellation());
	}

	public Configuration(float atmAtt, Set<Actions> mod, String DEM, String prefix, String path,
			String model, List<Constellation> consts) {
		atmOpticalThickness = atmAtt;
		mode = mod;
		fileNameDigitalElevationModel = DEM;
		filePrefixReport = prefix;
		filePathReport = path;
		fileNameScatterModel = model;
		constellations = consts;
	}

	public float getAtmOpticalThickness() {
		return atmOpticalThickness;
	}

	@Deprecated
	public List<Constellation> getConstellations() {
		return constellations;
	}

	public String getFileNameDigitalElevationModel() {
		return fileNameDigitalElevationModel;
	}

	public String getFileNameScatterModel() {
		return fileNameScatterModel;
	}

	public String getFilePathReport() {
		return filePathReport;
	}

	public String getFilePrefixReport() {
		return filePrefixReport;
	}

	public Set<Actions> getMode() {
		return mode;
	}

	public boolean hasAction(Actions a) {
		return mode.contains(a);
	}

}
