/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image;

// Java
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.IOException;

// FOP
import org.apache.fop.apps.Driver;
import org.apache.fop.messaging.*;
import org.apache.fop.datatypes.ColorSpace;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.image.analyser.ImageReader;
import org.apache.fop.image.analyser.EPSReader;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


/**
 * @see AbstractFopImage
 * @see FopImage
 */
public class EPSImage extends AbstractFopImage {
    private String docName;
    private int[] bbox;
    
    private byte[] epsImage = null;
    private EPSReader epsReader = null;
    
	/**
         * Initialize docName and bounding box
         */
    private void init(URL href) {
        bbox = new int[4];
        bbox[0] = 0;
        bbox[1] = 0;
        bbox[2] = 0;
        bbox[3] = 0;
        
        docName = href.toString();
    }
    
	/**
         * Return the name of the eps
         */
    public String getDocName() {
        return docName;
    }
    
	/**
         * Return the bounding box
         */
    public int[] getBBox() {
        return bbox;
    }
    
    public EPSImage(URL href) throws FopImageException {
        super(href);
        init(href);
    }
    
    public EPSImage(URL href,
                    ImageReader imgReader) throws FopImageException {
        super(href, imgReader);
        init(href);
        if (imgReader instanceof EPSReader) {
            EPSReader eimgReader = (EPSReader)imgReader;
            epsReader = eimgReader;
            epsImage = eimgReader.getEpsFile();
            m_bitmaps = epsImage;
            bbox = eimgReader.getBBox();
        }
    }
    
    protected void loadImage() throws FopImageException {
            // Image is loaded in reader
    }
    
    public byte[] getEPSImage() throws FopImageException {
       	if (epsImage == null)
            MessageHandler.errorln("ERROR LOADING EXTERNAL EPS");
        return epsImage;
    }
    
}
