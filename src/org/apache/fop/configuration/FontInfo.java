/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */


package org.apache.fop.configuration;

// Java
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Vector;

// Fop
import org.apache.fop.apps.FOPException;

/**
 * FontInfo contains meta information on fonts (where is the metrics file etc.)
 */

public class FontInfo {

    private String metricsFile, embedFile, name;
    private boolean kerning;
    private Vector fontTriplets;
    private String baseDir;

    public FontInfo(String name, String metricsFile, boolean kerning,
                    Vector fontTriplets, String embedFile) {
        this.name = name;
        this.metricsFile = metricsFile;
        this.embedFile = embedFile;
        this.kerning = kerning;
        this.fontTriplets = fontTriplets;
    }

    /**
     * @return the (absolute) file name of the metrics file
     */
    public String getMetricsFile() throws FOPException {
        // check if it's a URL and convert it to a filename
        try {
            metricsFile = new URL(metricsFile).getFile();
        } catch (MalformedURLException mue) {}

        // check if filename is absolute
        if ((new File(metricsFile).isAbsolute())) {
            return metricsFile;
        } else {
            return getBaseDir() + metricsFile;
        }
    }

    /**
     * @return the (absolute) file name of the font
     */
    public String getEmbedFile() throws FOPException {
        // check if it's a URL and convert it to a filename
        try {
            embedFile = new URL(embedFile).getFile();
        } catch (MalformedURLException mue) {}
        
        // check if filename is absolute
        if ((new File(embedFile).isAbsolute())) {
            return embedFile;
        } else {
            return getBaseDir() + embedFile;
        }
    }

    public boolean getKerning() {
        return kerning;
    }

    public Vector getFontTriplets() {
        return fontTriplets;
    }

    /**
     * @return BaseDir (path)
     */
    private String getBaseDir() throws FOPException {
        baseDir = Configuration.getStringValue("baseDir");
        URL baseURL = null;
        try {
            baseURL = new URL(baseDir);
        } catch (MalformedURLException mue) {
            // if the href contains only a path then file is assumed
            try {
                baseURL = new URL("file:" + baseDir);
            } catch (MalformedURLException mue2) {
                throw new FOPException("Error with baseDir: "
                                             + mue2.getMessage());
            }
        }
        baseDir = baseURL.getFile();
        return baseDir;
    }


}

