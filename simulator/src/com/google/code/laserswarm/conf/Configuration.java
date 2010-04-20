package com.google.code.laserswarm.conf;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import com.google.common.collect.Lists;

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
}
