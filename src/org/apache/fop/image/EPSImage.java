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

    private EPSData epsData = null;

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

    public EPSImage(URL href, FopImage.ImageInfo imgInfo) {
        super(href, imgInfo);
        init(href);
        if (imgInfo.data instanceof EPSData) {
            epsData = (EPSData) imgInfo.data;
            bbox = new int[4];
            bbox[0] = (int) epsData.bbox[0];
            bbox[1] = (int) epsData.bbox[1];
            bbox[2] = (int) epsData.bbox[2];
            bbox[3] = (int) epsData.bbox[3];

        }
    }

    public byte[] getEPSImage() {
        if (epsData.epsFile == null) {
            //log.error("ERROR LOADING EXTERNAL EPS");
        }
        return epsData.epsFile;
    }

    public static class EPSData {
        public long[] bbox;
        public boolean isAscii; // True if plain ascii eps file

        // offsets if not ascii
        public long psStart = 0;
        public long psLength = 0;
        public long wmfStart = 0;
        public long wmfLength = 0;
        public long tiffStart = 0;
        public long tiffLength = 0;

        /** raw eps file */
        public byte[] rawEps;
        /** eps part */
        public byte[] epsFile;
        public byte[] preview = null;
    }

}
