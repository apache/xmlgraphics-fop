/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */


package org.apache.fop.configuration;

// Java
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;

// Fop
import org.apache.fop.apps.FOPException;
import org.apache.fop.tools.URLBuilder;

/**
 * FontInfo contains meta information on fonts (where is the metrics file etc.)
 */

public class FontInfo {

    private String metricsFile, embedFile, name;
    private boolean kerning;
    private List fontTriplets;

    public FontInfo(String name, String metricsFile, boolean kerning,
                    List fontTriplets, String embedFile) {
        this.name = name;
        this.metricsFile = metricsFile;
        this.embedFile = embedFile;
        this.kerning = kerning;
        this.fontTriplets = fontTriplets;
    }

    /**
     * @return the URL to the metrics file
     */
    public URL getMetricsFile() throws FOPException {
        try {
            return URLBuilder.buildURL(Configuration.getFontBaseURL(), metricsFile);
        } catch (Exception e) {
            throw new FOPException("Invalid font metrics file: "+metricsFile+" ("+e.getMessage()+")");
        }
    }

    /**
     * @return the url to the font
     */
    public URL getEmbedFile() throws FOPException {
        // check if it's a URL and convert it to a filename
        if (embedFile == null) return null;
        try {
            return URLBuilder.buildURL(Configuration.getFontBaseURL(), embedFile);
        } catch (Exception e) {
            throw new FOPException("Invalid font file (embedFile): "+metricsFile+" ("+e.getMessage()+")");
        }
    }

    public boolean getKerning() {
        return kerning;
    }

    public List getFontTriplets() {
        return fontTriplets;
    }

}

