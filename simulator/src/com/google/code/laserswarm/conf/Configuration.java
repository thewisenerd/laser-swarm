package com.google.code.laserswarm.conf;

import java.io.File;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.google.common.collect.Lists;
import com.lyndir.lhunath.lib.system.logging.Logger;

@Root
public class Configuration {
	public static Configuration	instance;

	private static final String	configName						= "configuration.xml";

	@Attribute
	private String				mode							= "";

	@Attribute
	private float				atmOpticalThickness				= 0.25f;

	@Element
	private String				filePrefixReport				= "";

	@Element
	private String				filePathReport					= "";

	@Element
	private String				fileNameDigitalElevationModel	= "";

	@Element
	private String				fileNameScatterModel			= "";

	@ElementList
	private List<Constellation>	constellations					= Lists.newLinkedList();

	private static final Logger	logger							= Logger.get(Configuration.class);

	public static Configuration getInstance() {
		if (instance == null) {
			Configuration.read(configName);
		}
		return instance;
	}

	public static boolean read(String filename) {
		boolean success = true;
		Serializer serializer = new Persister();
		File source = new File(filename);
		try {
			instance = serializer.read(Configuration.class, source);
		} catch (Exception e) {
			success = false;
			logger.inf(e, "Configuration file reading failed");
		}
		return success;
	}

	public Configuration() {
		constellations.add(new Constellation());
		constellations.add(new Constellation());
	}

	public Configuration(float atmAtt, String mod, String DEM, String prefix, String path, String model,
			List<Constellation> consts) {
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

	public String getMode() {
		return mode;
	}

	public boolean write(String filename) {
		boolean success = true;
		Serializer serializer = new Persister();
		File result = new File(filename);
		try {
			serializer.write(instance, result);
		} catch (Exception e) {
			success = false;
			logger.inf(e, "Configuration file writing failed");
		}
		return success;
	}
}
