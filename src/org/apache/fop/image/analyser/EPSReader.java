/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.image.analyser;

// Java
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


// FOP
import org.apache.fop.messaging.*;
import org.apache.fop.image.SVGImage;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


/**
 * ImageReader object for SVG document image type.
 */
public class EPSReader extends AbstractImageReader {
    private long[] bbox;
    private boolean isAscii; // True if plain ascii eps file
    
	// offsets if not ascii
    long psStart = 0;
    long psLength = 0;
    long wmfStart = 0;
    long wmfLength = 0;
    long tiffStart = 0;
    long tiffLength = 0;
    
	/** raw eps file */
    private byte[] rawEps;
	/** eps part */
    private byte[] epsFile;
    private byte[] preview = null;
    
    private long getLong(byte[] buf, int idx) {
        int b1 = buf[idx] & 0xff;
        int b2 = buf[idx+1] & 0xff;
        int b3 = buf[idx+2] & 0xff;
        int b4 = buf[idx+3] & 0xff;
        
        return (long)((b4 << 24) | (b3 << 16) | (b2 << 8) | b1);
    }
    
    public boolean verifySignature(String uri, BufferedInputStream fis)
        throws IOException {
        boolean isEPS = false;
        this.imageStream = fis;
        fis.mark(32);
        byte[] header = new byte[30];
        fis.read(header, 0, 30);
        fis.reset();
        
            // Check if binary header
        if (getLong(header, 0) == 0xC6D3D0C5) {
            isAscii = false;
            isEPS = true;
            
            psStart = getLong(header, 4);
            psLength = getLong(header, 8);
            wmfStart = getLong(header, 12);
            wmfLength = getLong(header, 16);
            tiffStart = getLong(header, 20);
            tiffLength = getLong(header, 24);
            
        } else {
                // Check if plain ascii
            byte[] epsh = "%!PS".getBytes();
            if (epsh[0] == header[0] &&
                epsh[1] == header[1] &&
                epsh[2] == header[2] &&
                epsh[3] == header[3]) {
                isAscii = true;
                isEPS = true;
            }
        }
        
        if (isEPS) {
            readEPSImage(fis);
            bbox = readBBox();
            
            if (bbox != null) {
                width = (int)(bbox[2]-bbox[0]);
                height = (int)(bbox[3]-bbox[1]);
            } else {
		 // Ain't eps if no BoundingBox
                isEPS = false;
            }
        }
        
        return isEPS;
    }
    
	/** read the eps file and extract eps part */
    private void readEPSImage(BufferedInputStream fis) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] file;
        byte[] readBuf = new byte[20480];
        int bytes_read;
        int index = 0;
        boolean cont = true;
        
        
        try {
            while ((bytes_read = fis.read(readBuf)) != -1) {
                baos.write(readBuf, 0, bytes_read);
            }
        } catch (java.io.IOException ex) {
            throw new IOException("Error while loading EPS image " + ex.getMessage());
        }
        
        file = baos.toByteArray();
        
        if (isAscii) {
            rawEps = null;
            epsFile = new byte[file.length];
            System.arraycopy(file, 0, epsFile, 0, epsFile.length);
        } else {
            rawEps = new byte[file.length];
            epsFile = new byte[(int)psLength];
            System.arraycopy(file, 0, rawEps, 0, rawEps.length);
            System.arraycopy(rawEps, (int)psStart, epsFile, 0, (int)psLength);
        }
    }
    
    public byte[] getEpsFile() {
        return epsFile;
    }
    
	/* Get embedded preview or null */
    public byte[] getPreview() {
        InputStream is = null;
        if (preview == null) {
            if (tiffLength > 0) {
                preview = new byte[(int)tiffLength];
                System.arraycopy(rawEps, (int)tiffStart, preview, 0, (int)tiffLength);
            }
        }
        return preview;
    }
    
	/** Extract bounding box from eps part
         */
    private long[] readBBox() {
        long[] mbbox = null;
        int idx = 0;
        byte[] bbxName = "%%BoundingBox: ".getBytes();
        boolean found = false;
        
        while (!found && (epsFile.length  > (idx + bbxName.length))) {
            boolean sfound = true;
            int i = idx;
            for (i = idx; sfound && (i-idx) < bbxName.length; i++) {
                if (bbxName[i - idx] != epsFile[i])
                    sfound = false;
            }
            if (sfound) {
                found = true;
                idx = i;
            } else {
                idx++;
            }
        }
        
        if (!found)
            return mbbox;
        
        
        mbbox = new long[4];
        idx += readLongString(mbbox, 0, idx);
        idx += readLongString(mbbox, 1, idx);
        idx += readLongString(mbbox, 2, idx);
        idx += readLongString(mbbox, 3, idx);
        
        return mbbox;
    }
    
    private int readLongString(long[] mbbox, int i, int idx) {
        while (idx < epsFile.length &&
               (epsFile[idx] == 32))
            idx++;
        
        int nidx = idx;
        
        while (nidx < epsFile.length &&
               (epsFile[nidx] >= 48 && epsFile[nidx] <= 57))
            nidx++;
        
        byte[] num = new byte[nidx - idx];
        System.arraycopy(epsFile, idx, num, 0, nidx-idx);
        String ns = new String(num);
        mbbox[i] = Long.parseLong(ns);
        
        return (1+nidx - idx);
    }
    
    public String getMimeType() {
        return "image/eps";
    }
    
	/**
         * Return the BoundingBox
         */
    public int[] getBBox() {
        int[] bbox = new int[4];
        bbox[0] = (int)this.bbox[0];
        bbox[1] = (int)this.bbox[1];
        bbox[2] = (int)this.bbox[2];
        bbox[3] = (int)this.bbox[3];
        return bbox;
    }
}

