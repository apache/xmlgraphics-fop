/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
 */

//Author:       Eric SCHAEFFER
//Description:  implement FopImage using Jimi

package org.apache.fop.image;

import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.pdf.PDFFilter;

// Java
import java.util.Hashtable;
import java.net.URL;
import java.io.IOException;
//import java.io.PrintWriter;
import java.awt.image.*;
import java.awt.*;

import java.lang.reflect.Array;

// Jimi
import com.sun.jimi.core.*;

public class JimiImage implements FopImage {

	int m_width = 0;
	int m_height = 0;
	URL m_href = null;
	ColorSpace m_colorSpace = null;
	int m_bitsPerPixel = 0;
	byte[] m_bitmaps = null;
	int m_bitmapsSize = 0;
	boolean m_isTransparent = false;
	PDFColor m_transparentColor = null;

	// Init the object.
	// If href protocol isn't file://, can load the entire image
	// and keep it in memory.
	// Should cache the input stream, and load data when needed.
	public JimiImage(URL href) throws FopImageException {
		this.m_href = href;
		
	}

	protected void getImage() throws FopImageException {
		int[] tmpMap = null;
		try {
			ImageProducer ip = Jimi.getImageProducer(this.m_href, Jimi.SYNCHRONOUS | Jimi.IN_MEMORY);
			FopImageConsumer consumer = new FopImageConsumer(this);
			ip.startProduction(consumer);

			while (! consumer.isImageReady() ) {}
			this.m_height = consumer.getHeight();
			this.m_width = consumer.getWidth();

			tmpMap = new int[this.m_width * this.m_height];
			PixelGrabber pg = new PixelGrabber(
										ip,
										0, 0,
										this.m_width, this.m_height,
										tmpMap, 
										0, this.m_width
										);
			try {
				pg.grabPixels();
			} catch (InterruptedException intex) {
				throw new FopImageException("Image grabbing interrupted : " + intex.getMessage());
			}
		} catch (Exception ex) {
			throw new FopImageException("Error while loading image " + this.m_href.toString() + " : " + ex.getClass() + " - " + ex.getMessage());
		}

		// Should take care of the ColorSpace and bitsPerPixel
		this.m_bitmaps = new byte[this.m_width * this.m_height * 3];
		for (int i = 0; i < this.m_height; i++) {
			for (int j = 0; j < this.m_width; j++) {
				int p = tmpMap[i * this.m_width + j];
				int r = (p >> 16) & 0xFF;
				int g = (p >>  8) & 0xFF;
				int b = (p      ) & 0xFF;
				this.m_bitmaps[3 * (i * this.m_width + j)] = (byte) (r & 0xFF);
				this.m_bitmaps[3 * (i * this.m_width + j) + 1] = (byte) (g & 0xFF);
				this.m_bitmaps[3 * (i * this.m_width + j) + 2] = (byte) (b & 0xFF);
			}
		}
		this.m_bitmapsSize = java.lang.reflect.Array.getLength(this.m_bitmaps);

		this.m_bitsPerPixel = 8;
		this.m_colorSpace = new ColorSpace(ColorSpace.DEVICE_RGB);
	}

	// Get image general properties.
	// Methods throw exception because they can retrieve data
	// when needed.

	// Ressource location
	public String getURL() {
		return this.m_href.toString();
	}

	// image size
	public int getWidth() throws FopImageException {
		if (this.m_width == 0) this.getImage();

		return this.m_width;
	}

	public int getHeight() throws FopImageException {
		if (this.m_height == 0) this.getImage();

		return this.m_height;
	}

	// DeviceGray, DeviceRGB, or DeviceCMYK
	public ColorSpace getColorSpace() throws FopImageException {
		if (this.m_colorSpace == null) this.getImage();

		return this.m_colorSpace;
	}

	// bits per pixel
	public int getBitsPerPixel() throws FopImageException {
		if (this.m_bitsPerPixel == 0) this.getImage();

		return this.m_bitsPerPixel;
	}

	// For transparent images
	public boolean isTransparent() throws FopImageException {
		return false;
	}

	public PDFColor getTransparentColor() throws FopImageException {
		return null;
	}

	// get the image bytes, and bytes properties

	// get uncompressed image bytes
	public byte[] getBitmaps() throws FopImageException {
		if (this.m_bitmaps == null) this.getImage();

		return this.m_bitmaps;
	}
// width * (bitsPerPixel / 8) * height, no ?
	public int getBitmapsSize() throws FopImageException {
		if (this.m_bitmapsSize == 0) this.getImage();

		return this.m_bitmapsSize;
	}

	// get compressed image bytes
	// I don't know if we really need it, nor if it
	// should be changed...
	public byte[] getRessourceBytes() throws FopImageException {
		return null;
	}
	public int getRessourceBytesSize() throws FopImageException {
		return 0;
	}
	// return null if no corresponding PDFFilter
	public PDFFilter getPDFFilter() throws FopImageException {
		return null;
	}

	// release memory
	public void close() {
		/* For the moment, only release the bitmaps (image areas
		   can share the same FopImage object)
		   Thus, even if it had been called, other properties
		   are still available.
		*/
//		this.m_width = 0;
//		this.m_height = 0;
//		this.m_href = null;
//		this.m_colorSpace = null;
//		this.m_bitsPerPixel = 0;
		this.m_bitmaps = null;
		this.m_bitmapsSize = 0;
//		this.m_isTransparent = false;
//		this.m_transparentColor = null;
	}

	// CONSUMER CLASS
	public static class FopImageConsumer implements ImageConsumer {
		int width = -1;
		int height = -1;
		JimiImage graphic;
		Integer imageStatus = new Integer(-1);

		public FopImageConsumer(JimiImage graphic) {
			this.graphic = graphic;
		}

		public void imageComplete(int status) {
			synchronized (this.imageStatus) {
				this.imageStatus = new Integer(status);
			}
		}

		public synchronized boolean isImageReady()
			throws Exception {
			synchronized (this.imageStatus) {
				if ( this.imageStatus.intValue() != -1 ) {
					String statusStr = null;
					if ( this.imageStatus.intValue() == ImageConsumer.IMAGEABORTED ) {
						statusStr = new String("Image Aborted");
					} else if ( this.imageStatus.intValue() == ImageConsumer.IMAGEERROR ) {
						statusStr = new String("Image Error");
					}
					if ( statusStr != null ) {
						throw new Exception("Error in image consumer (" + statusStr + ")");
					}

					if ( this.imageStatus.intValue() == ImageConsumer.STATICIMAGEDONE ) {
						return true;
					}
				}
				return false;
			}
		}

		public void setColorModel(ColorModel model) {}

		public void setDimensions(int width, int height) {
			this.width = width;
			this.height = height;
		}

		public void setHints(int hintflags) {}

		public void setPixels(int x, int y, int w, int h,
		   ColorModel model, byte[] pixels,int off,
		   int scansize) {}

		public void setPixels(int x, int y, int w, int h,
		   ColorModel model, int[] pixels, int off,
		   int scansize) {}

		public void setProperties(Hashtable props) {}

		public int getWidth() {
			return this.width;
		}

		public int getHeight() {
			return this.height;
		}
	}
}
