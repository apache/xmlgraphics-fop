/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.pdf;

import java.util.ArrayList;

/**
 * FontInfo contains meta information on fonts (where is the metrics file etc.)
 */
public class EmbedFontInfo {
    private String metricsFile, embedFile;
    private boolean kerning;
    private ArrayList fontTriplets;

    public EmbedFontInfo(String metricsFile, boolean kerning,
                    ArrayList fontTriplets, String embedFile) {
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

    public ArrayList getFontTriplets() {
        return fontTriplets;
    }

}

