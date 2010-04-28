package com.google.code.laserswarm.conf;

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

	@Attribute
	private static Set<Actions>		mode;
	static {
		mode = Sets.newHashSet();
		mode.add(Actions.SIMULATE);
		mode.add(Actions.PROCESS);
		mode.add(Actions.PLOT_DISK);
		mode.add(Actions.TABULATE);
	}

	@Element
	private int						simThreads						= 4;

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
		String name = getInstance().configName;
		logger.inf("Removing %s", name);
		try {
			(new File(name)).delete();
		} catch (Exception e) {
		}
		logger.inf("Making a new cfg and saving");
		getInstance().write(name);
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

	public int getSimThreads() {
		return simThreads;
	}

	public boolean hasAction(Actions a) {
		return mode.contains(a);
	}

}
