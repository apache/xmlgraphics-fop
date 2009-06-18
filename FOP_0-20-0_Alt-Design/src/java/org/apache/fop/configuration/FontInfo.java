/*
 * $Id$
 * 
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 */


package org.apache.fop.configuration;

import java.util.ArrayList;

/**
 * FontInfo contains meta information on fonts (where is the metrics file etc.)
 */

public class FontInfo {
    private String metricsFile, embedFile, name;
    private boolean kerning;
    private ArrayList fontTriplets;

    public FontInfo(String name, String metricsFile, boolean kerning,
                    ArrayList fontTriplets, String embedFile) {
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

    public ArrayList getFontTriplets() {
        return fontTriplets;
    }

}

