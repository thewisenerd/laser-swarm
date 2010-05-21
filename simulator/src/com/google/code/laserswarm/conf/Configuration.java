package com.google.code.laserswarm.conf;

import jat.spacetime.CalDate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import com.google.common.collect.Sets;
import com.lyndir.lhunath.lib.system.logging.Logger;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

public class Configuration {

	public enum Actions {
		SIMULATE,
		PROCESS,
		TABULATE,
		PLOT_DISK,
		PLOT_SCREEN,
		SLEEP,
		PROSPECT,
		COUNT_ONLY,
		FORCE_FLAT,
		CONSTANT_SCATTER;
	}

	private transient static Configuration	instance;

	private transient static final String	configName			= "configuration.xml";

	/**
	 * Location of the DEMs
	 */
	public transient static File			demDir				= new File("DEM");
	public transient static File			volitileCache		= new File("cache/volitile");
	static {
		volitileCache.mkdirs();
		for (File subFile : volitileCache.listFiles())
			subFile.delete();
	}
	public transient static File			nonVolitileCache	= new File("cache/nonVolitile");
	static {
		nonVolitileCache.mkdirs();
	}
	/**
	 * Earth radius based on EPSG:3785 ellipsoid (spheroid)
	 */
	public transient static double			R0					= 6378137;
	/**
	 * Orbit epoch date
	 */
	public transient static final CalDate	epoch				= new CalDate(2000, 7, 1, 0, 0, 0);
	/**
	 * Stefan-Boltzmann constant
	 */
	public transient static double			sigma				= 5.67E-8;
	/**
	 * Planck's constant
	 */
	public transient static double			h					= 6.626068E-34;
	/**
	 * Light speed
	 */
	public transient static double			c					= 299792458;
	/**
	 * Boltzmann constant
	 */
	public transient static double			k					= 1.3806503E-23;
	/**
	 * Sun's grey body temperature
	 */
	public transient static double			TSun				= 5800;
	/**
	 * Sun's grey body emissivity
	 */
	public transient static double			epsSun				= 0.99;
	/**
	 * Earth's grey body temperature
	 */
	public transient static double			TEarth				= 254.356;
	/**
	 * Earth's grey body emissivity
	 */
	public transient static double			epsEarth			= 1;

	private static Set<Actions>				mode;
	static {
		mode = Sets.newHashSet();
		mode.add(Actions.SIMULATE);
		mode.add(Actions.PROCESS);
		mode.add(Actions.PLOT_DISK);
		mode.add(Actions.TABULATE);
	}

	public static int						simThreads			= 9;
	public static int						demThreads			= 4;

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

	private static void setInstance(Configuration instance) {
		Configuration.instance = instance;
	}

	public static void setMode(Set<Actions> mode) {
		Configuration.mode = mode;
	}

	public static void write() {
		write(configName);
	}

	public static void write(String filename) {
		write(filename, getInstance());
	}

	/**
	 * Serialize a given object
	 * 
	 * @param <T>
	 *            Type of the object
	 * @param filename
	 *            File to save to
	 * @param object
	 *            Object to save
	 */
	public static <T> void write(String filename, T object) {
		logger.dbg("Trying to save the configuration");
		try {
			File file = new File(filename);
			FileWriter writer = new FileWriter(file);
			XStream xstream = getDefaultSerializer(filename);
			xstream.toXML(object, writer);
			writer.close();
		} catch (IOException e) {
			logger.err(e, "IO Exeption, DID NOT SAVE CONFIG");
		}
	}

	private float				atmOpticalThickness				= 0.25f;

	private String				filePrefixReport				= "";

	private String				filePathReport					= "./";

	private String				fileNameDigitalElevationModel	= "";

	private String				fileNameScatterModel			= "";

	private static final Logger	logger							= Logger.get(Configuration.class);

	public static XStream getDefaultSerializer(String name) {
		XStream xstream;
		if (name.endsWith(".json")) {
			logger.inf("Loading from %s as JSON", name);
			xstream = new XStream(new JsonHierarchicalStreamDriver());
		} else {
			logger.inf("Loading from %s as XML", name);
			xstream = new XStream();
		}
		return xstream;
	}

	public static Configuration getInstance() {
		if (instance == null) {
			Configuration cfg;
			try {
				cfg = Configuration.read();
			} catch (Exception e) {
				logger.inf(e, "Loading default config");
				cfg = new Configuration();
			}
			setInstance(cfg);
		}
		return instance;
	}

	public static Configuration read() throws FileNotFoundException {
		return read(configName);
	}

	public static Configuration read(String cfgname) throws FileNotFoundException {
		Configuration cfg = read(cfgname, getDefaultSerializer(cfgname));
		setInstance(cfg);
		return cfg;
	}

	public static <T> T read(String name, XStream xstream) throws FileNotFoundException {
		T obj = null;
		try {
			File file = new File(name);
			FileReader reader = new FileReader(file);
			obj = (T) xstream.fromXML(reader);
			reader.close();
		} catch (FileNotFoundException e) {
			logger.wrn(e, "File %s not found; Configuration not loaded", name);
			throw e;
		} catch (IOException e) {
			logger.err(e, "Read problem while loading from: %s", name);
		}
		return obj;
	}

	public Configuration() {
		// TODO Auto-generated constructor stub
	}

	public Configuration(float atmAtt, Set<Actions> mod, String DEM, String prefix, String path,
			String model) {
		atmOpticalThickness = atmAtt;
		mode = mod;
		fileNameDigitalElevationModel = DEM;
		filePrefixReport = prefix;
		filePathReport = path;
		fileNameScatterModel = model;
	}

	public float getAtmOpticalThickness() {
		return atmOpticalThickness;
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
