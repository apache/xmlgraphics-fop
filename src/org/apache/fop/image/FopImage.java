/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

//Author:       Eric SCHAEFFER
//Description:  represent an image object

package org.apache.fop.image;

import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFFilter;

public interface FopImage {
	// Init the object.
	// If href protocol isn't file://, can load the entire image
	// and keep it in memory.
	// Should cache the input stream, and load data when needed.
//	public FopImage(URL href) throws FopImageException;

	// Get image general properties.
	// Methods throw exception because they can retrieve data
	// when needed.

	// Ressource location
	public String getURL();

	// image size
	public int getWidth() throws FopImageException;
	public int getHeight() throws FopImageException;

	// DeviceGray, DeviceRGB, or DeviceCMYK
	public ColorSpace getColorSpace() throws FopImageException;

	// bits per pixel
	public int getBitsPerPixel() throws FopImageException;

	// For transparent images
	public boolean isTransparent() throws FopImageException;
	public PDFColor getTransparentColor() throws FopImageException;

	// get the image bytes, and bytes properties

	// get uncompressed image bytes
	public byte[] getBitmaps() throws FopImageException;
// width * (bitsPerPixel / 8) * height, no ?
	public int getBitmapsSize() throws FopImageException;

	// get compressed image bytes
	// I don't know if we really need it, nor if it
	// should be changed...
	public byte[] getRessourceBytes() throws FopImageException;
	public int getRessourceBytesSize() throws FopImageException;
	// return null if no corresponding PDFFilter
	public PDFFilter getPDFFilter() throws FopImageException;

	// release memory
	public void close();
}
