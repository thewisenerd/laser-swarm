package com.google.code.laserswarm.util.demReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;

import com.google.code.laserswarm.earthModel.ElevationModel;

public class GeoTiffParser extends DemReader {

	public static void main(String[] args) {
		try {
			new GeoTiffParser(new File("C:/Users/simon/Desktop/ASTER GDEM BE/ASTGTM_N48E000_dem.tif"))
					.parse();
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
		GridCoverage2D coverage = null;
		try {
			coverage = reader.read(null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// StyleBuilder styleBuilder = new StyleBuilder();
		// RasterSymbolizer rastSymbolizer = styleBuilder.createRasterSymbolizer();
		// Style style = styleBuilder.createStyle(rastSymbolizer);

		// try {
		// MapContext context = new DefaultMapContext();
		// context.addLayer(coverage, style);
		//
		// JMapPane pane = new JMapPane(new StreamingRenderer(), context);
		//
		// JFrame fr = new JFrame();
		// fr.setSize(800, 600);
		// fr.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// fr.add(pane);
		// fr.setVisible(true);
		// } catch (FactoryRegistryException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		return new ElevationModel(getDemFile(), null, coverage);
	}
}
