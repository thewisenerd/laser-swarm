package com.google.code.laserswarm.util.demReader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.ujmp.core.doublematrix.impl.ImageMatrix;

import com.google.code.laserswarm.earthModel.ElevationModel;
import com.google.code.laserswarm.out.plot2D.Plot2D;

public class GeoTiffParser extends DemReader {

	public static void main(String[] args) {
		try {
			new GeoTiffParser(new File("DEM/ASTGTM_N48E000_dem.tif")).parse();
		} catch (DemCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public GeoTiffParser(File demFile) {
		super(demFile);
	}

	@Override
	public ElevationModel parse() throws DemCreationException {
		FileInputStream file = null;
		try {
			file = new FileInputStream(getDemFile());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Get the channel
		FileChannel fc = file.getChannel();

		// Open the GeoTiff reader
		GeoTiffReader reader;
		try {
			// This function always allocates about 23Mb, both for 2Mb and 225Mb
			System.out.println("Start reading");
			reader = new GeoTiffReader(fc, new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER,
					Boolean.TRUE));
			System.out.println("Done reading");
		} catch (DataSourceException ex) {
			ex.printStackTrace();
			return null;
		}

		// Get the image properties
		GridCoverage2D coverage;
		try {
			coverage = reader.read(null);
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}

		BufferedImage bufferedIm = Plot2D.mkImage(coverage);
		ImageMatrix m;
		try {
			m = new ImageMatrix(bufferedIm);
			m.showGUI();
			Plot2D.make(m);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
