/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.image;


/**
 * EPS image handler.
 * This handles the Encapulated PostScript images.
 * It gets the dimensions and original data from the analyser.
 *
 * @see AbstractFopImage
 * @see FopImage
 */
public class EPSImage extends AbstractFopImage {
    
    private String docName;
    private int[] bbox;

    private EPSData epsData = null;

    /**
     * Create an EPS image with the image information.
     *
     * @param imgInfo the information containing the data and bounding box
     */
    public EPSImage(FopImage.ImageInfo imgInfo) {
        super(imgInfo);
        init("");
        if (imgInfo.data instanceof EPSData) {
            epsData = (EPSData) imgInfo.data;
            bbox = new int[4];
            bbox[0] = (int) epsData.bbox[0];
            bbox[1] = (int) epsData.bbox[1];
            bbox[2] = (int) epsData.bbox[2];
            bbox[3] = (int) epsData.bbox[3];

            loaded = loaded | ORIGINAL_DATA;
        }
    }

    /**
     * Initialize docName and bounding box.
     * @param name the document name
     */
    private void init(String name) {
        bbox = new int[4];
        bbox[0] = 0;
        bbox[1] = 0;
        bbox[2] = 0;
        bbox[3] = 0;

        docName = name;
    }

    /**
     * Return the name of the eps
     * @return the name of the eps
     */
    public String getDocName() {
        return docName;
    }

    /**
     * Return the bounding box
     * @return an int array containing the bounding box
     */
    public int[] getBBox() {
        return bbox;
    }

    /**
     * Get the eps image.
     *
     * @return the original eps image data
     */
    public byte[] getEPSImage() {
        if (epsData.epsFile == null) {
            //log.error("ERROR LOADING EXTERNAL EPS");
        }
        return epsData.epsFile;
    }

    /**
     * Data for EPS image.
     */
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
