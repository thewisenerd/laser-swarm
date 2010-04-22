package com.google.code.laserswarm.util;


public class GeoTiff {

	// public static PlanarImage loadGeoTiffImage(String fileName, int[] imageSize, double[][] range,
	// double[] gridSize, Map<String, String> coordSys) {
	//
	// // Get the file
	// FileInputStream file = null;
	// try {
	// file = new FileInputStream(fileName);
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// }
	//
	// // Get the channel
	// FileChannel fc = file.getChannel();
	//
	// // Open the GeoTiff reader
	// GeoTiffReader reader;
	// try {
	// // This function always allocates about 23Mb, both for 2Mb and 225Mb
	// System.out.println("Start reading");
	// reader = new GeoTiffReader(fc, new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER,
	// Boolean.TRUE));
	// System.out.println("Done reading");
	// } catch (DataSourceException ex) {
	// ex.printStackTrace();
	// return null;
	// }
	//
	// // Get the image properties
	// GridCoverage2D coverage;
	// try {
	// coverage = reader.read(null);
	// } catch (IOException ex) {
	// ex.printStackTrace();
	// return null;
	// }
	// RenderedImage renderedImage = coverage.getRenderedImage();
	// CoordinateReferenceSystem crs = coverage.getCoordinateReferenceSystem2D();
	// Envelope env = coverage.getEnvelope();
	//
	// System.out.println(crs.toString());
	//
	// // Size
	// imageSize[0] = renderedImage.getWidth();
	// imageSize[1] = renderedImage.getHeight();
	//
	// // Range
	// range[0][0] = env.getMinimum(0);
	// range[0][1] = env.getMaximum(0);
	// range[1][0] = env.getMinimum(1);
	// range[1][1] = env.getMaximum(1);
	//
	// // Grid size
	// gridSize[0] = env.getLength(0) / (imageSize[0] - 1);
	// gridSize[1] = env.getLength(1) / (imageSize[1] - 1);
	//
	// // Get the coordinate system information
	// // parseCoordinateSystem(crs.toWKT(), coordSys);
	//
	// // Return planar image
	// return PlanarImage.wrapRenderedImage(renderedImage);
	// }

}
