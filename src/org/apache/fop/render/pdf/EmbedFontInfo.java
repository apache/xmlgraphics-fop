/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.pdf;

import java.util.List;

/**
 * FontInfo contains meta information on fonts (where is the metrics file etc.)
 */
public class EmbedFontInfo {
    
    private String metricsFile, embedFile;
    private boolean kerning;
    private List fontTriplets;

    /**
     * Main constructor
     * @param metricsFile Path to the xml file containing font metrics
     * @param kerning True if kerning should be enabled
     * @param fontTriplets List of font triplets to associate with this font
     * @param embedFile Path to the embeddable font file (may be null)
     */
    public EmbedFontInfo(String metricsFile, boolean kerning,
                    List fontTriplets, String embedFile) {
        this.metricsFile = metricsFile;
        this.embedFile = embedFile;
        this.kerning = kerning;
        this.fontTriplets = fontTriplets;
    }

    /**
     * Returns the path to the metrics file
     * @return the metrics file path
     */
    public String getMetricsFile() {
        return metricsFile;
    }

    /**
     * Returns the path to the embeddable font file
     * @return the font file path
     */
    public String getEmbedFile() {
        return embedFile;
    }

    /**
     * Determines if kerning is enabled
     * @return True if enabled
     */
    public boolean getKerning() {
        return kerning;
    }

    /**
     * Returns the list of font triplets associated with this font.
     * @return List of font triplets
     */
    public List getFontTriplets() {
        return fontTriplets;
    }

}

