package com.google.code.laserswarm.earthModel;

public class EarthModel {

	private ElevationModel				elevationModel;
	private ScatteringCharacteristics	scatteringCharacteristics;

	public ElevationModel getElevationModel() {
		return elevationModel;
	}

	public ScatteringCharacteristics getScatteringCharacteristics() {
		return scatteringCharacteristics;
	}

	public void setElevationModel(ElevationModel elevationModel) {
		this.elevationModel = elevationModel;
	}

	public void setScatteringCharacteristics(ScatteringCharacteristics scatteringCharacteristics) {
		this.scatteringCharacteristics = scatteringCharacteristics;
	}

}
