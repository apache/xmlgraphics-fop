/*-- $Id$ -- 

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
 
 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
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

/* originally contributed by Sergio Botti, with modifications by James
   Tauber and Kelly Campbell */

package org.apache.fop.image;

import java.util.Hashtable;
import java.net.URL;
import java.io.IOException;
import java.io.PrintWriter;
import java.awt.image.*;
import java.awt.Image;
import java.awt.Toolkit;

public class GifJpegImage implements FopImage {

    int X;
    int Y;
    int width;
    int height;
    int pixelwidth;
    int pixelheight;
    String ref;
    boolean color = true;
    int bitperpixel = 8;
    int[] imagemap;
    int[] tempmap;

    /** synchronization object */
    protected Integer imageWait = new Integer(0);

    public GifJpegImage(String href, int x, int y, int w, int h) {

	this.ref = href;
	this.X = x;
	this.Y = y;
	this.pixelheight = -1;
	this.pixelwidth = -1;

	try {
	    URL url = new URL(href);
	    ImageProducer ip = (ImageProducer)url.getContent();
	    FopImageConsumer consumer = new FopImageConsumer(this);
	    ip.startProduction(consumer);
	    synchronized (imageWait) {
		imageWait.wait();
	    }

	    while ((this.pixelheight = consumer.getHeight())==-1) {}
	    while ((this.pixelwidth = consumer.getWidth())==-1) {}

	    this.tempmap = new int[this.pixelwidth * this.pixelheight];

	    PixelGrabber pg = new
		PixelGrabber(ip, 0, 0,
			     this.pixelwidth, this.pixelheight,
			     this.tempmap, 0, this.pixelwidth);
	    try {
		pg.grabPixels();
	    } catch (InterruptedException e) {
		System.err.println("Image grabbing interrupted"); 
	    }
	} catch (ClassCastException e) {
	    System.err.println("Image format not supported: " + href);
	} catch (Exception e) {
	    System.err.println("Error loading image " + href +
			       " : " + e);
	}

	if (w==0) {
	    this.width=this.pixelwidth*1000;
	} else {
	    this.width=w;
	}

	if (h==0) {
	    this.height=this.pixelheight*1000;
	} else {
	    this.height=h;
	}
    }

    public static class FopImageConsumer implements ImageConsumer {
	int width = -1;
	int height = -1;
	GifJpegImage graphic;

	public FopImageConsumer(GifJpegImage graphic) {
	    this.graphic = graphic;
	}

	public void imageComplete(int status) {
	    synchronized(graphic.imageWait) {
		graphic.imageWait.notifyAll();
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

    public String gethref() {
	return this.ref;
    }

    public int getWidth() {
	return this.width;
    }

    public int getHeight() {
	return this.height;
    }

    public int getpixelwidth() {
	return this.pixelwidth;
    }

    public int getpixelheight() {
	return this.pixelheight;
    }

    public int getX() {
	return this.X;
    }

    public int getY() {
	return this.Y;
    }

    public int[] getimagemap() {
	this.imagemap=new int[this.pixelheight * this.pixelwidth * 3];
	int count = 0;
        int x = 0;
        int y = 0;

        for ( y = (this.pixelheight - 1) * this.pixelwidth; y >= 0; y -= this.pixelwidth) {
            for ( x = 0; x < this.pixelwidth; x++) {

                int p = this.tempmap[y + x];

                this.imagemap[count++] = ((p >>16) & 0xff);
                this.imagemap[count++] = ((p >> 8) & 0xff);
                this.imagemap[count++] = (p & 0xff);
            }    
	}
	return imagemap;
    }

    public boolean getcolor() {
	return true;
    }

    public int getbitperpixel() {
	return this.bitperpixel;
    }
}


