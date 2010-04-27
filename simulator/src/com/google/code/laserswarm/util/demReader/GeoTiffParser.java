package com.google.code.laserswarm.util.demReader;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.google.code.laserswarm.earthModel.ElevationModel;

public class GeoTiffParser extends DemReader {

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
		RenderedImage renderedImage = coverage.getRenderedImage();
		CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem2D();
		Envelope env = coverage.getEnvelope();

		System.out.println(crs.toString());

		// PlanarImage i = PlanarImage.wrapRenderedImage(renderedImage);

		BufferedImage mine = new BufferedImage(renderedImage.getWidth(), renderedImage.getHeight(),
				BufferedImage.TYPE_BYTE_GRAY);

		StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
		Style style = styleFactory.createStyle();

		coverage.getGridGeometry().getGridRange2D();
		return null;
	}
}
