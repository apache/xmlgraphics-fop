/*
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */


package org.apache.fop.configuration;

import java.util.Vector;

/**
 * FontInfo contains meta information on fonts (where is the metrics file etc.)
 */

public class FontInfo {
    private String metricsFile, embedFile, name;
    private boolean kerning;
    private Vector fontTriplets;

    public FontInfo (String name, String metricsFile, boolean kerning,
                     Vector fontTriplets, String embedFile) {
        this.name = name;
        this.metricsFile = metricsFile;
        this.embedFile = embedFile;
        this.kerning = kerning;
        this.fontTriplets = fontTriplets;
    }

    public String getMetricsFile() {
        return metricsFile;
    }
    public String getEmbedFile() {
        return embedFile;
    }
    public boolean getKerning() {
        return kerning;
    }
    public Vector getFontTriplets() {
        return fontTriplets;
    }

}

