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
	@Attribute
	private String mode = "";
	
	@Attribute
	private float atmosphericAttenuation = 0f;
	
	@Element
	private String fileNameReport = "";
	
	@Element
	private String fileNameDigitalElevationModel = "";
	
	@Element
	private String fileNameScatterModel = "";
	
	@ElementList
	private List<Constellation> constellations = Lists.newLinkedList();
	
	public float getAtmosphericAttenuation() {
		return atmosphericAttenuation;
	}
	public String getMode() {
		return mode;
	}
	public String getFileNameDigitalElevationModel() {
		return fileNameDigitalElevationModel;
	}
	public String getFileNameReport() {
		return fileNameReport;
	}
	public String getFileNameScatterModel() {
		return fileNameScatterModel;
	}
	public List<Constellation> getConstellations() {
		return constellations;
	}
	
	private static final Logger	logger	= Logger.get(Configuration.class);
	
	public Configuration() {
		constellations.add(new Constellation());
		constellations.add(new Constellation());
	}
	
	public Configuration(float atmAtt, String mod, String DEM, String report, String model, List<Constellation> consts) {
		atmosphericAttenuation = atmAtt;
		mode = mod;
		fileNameDigitalElevationModel = DEM;
		fileNameReport = report;
		fileNameScatterModel = model;
		constellations = consts;
	}
	
	public boolean write(String filename)
	{
		boolean success = true;
		Serializer serializer = new Persister();
		File result = new File(filename);
		try {
			serializer.write(this, result);
		} catch (Exception e) {
			success = false;
			logger.inf(e, "Configuration file writing failed");
		}
		return success;
	}
	
	public boolean read(String filename)
	{
		return true;
	}
}
