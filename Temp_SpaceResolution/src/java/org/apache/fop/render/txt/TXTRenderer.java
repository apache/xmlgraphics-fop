/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */
 
package org.apache.fop.render.txt;

// FOP
import org.apache.fop.render.PrintRenderer;
import org.apache.fop.render.pcl.PCLStream;

/**
 * Renderer that renders areas to plain text
 *
 * Created by Arthur E Welch III while at M&I EastPoint Technology
 * Modified by Mark Lillywhite mark-fop@inomial.com to use the new
 * Renderer interface.
 */
public class TXTRenderer extends PrintRenderer {

    /** The MIME type for PostScript */
    public static final String MIME_TYPE = "text/plain";

    /**
     * the current stream to add Text commands to
     */
    private PCLStream currentStream;

    private int pageHeight = 7920;

    // These variables control the virtual paggination functionality.
    private int curdiv = 0;
    private int divisions = -1;
    private int paperheight = -1;    // Paper height in decipoints?
    private int orientation = -1;    // -1=default/unknown, 0=portrait, 1=landscape.
    private int topmargin = -1;      // Top margin in decipoints?
    private int leftmargin = -1;     // Left margin in decipoints?
    private int fullmargin = 0;
    private final boolean debug = false;

    // Variables for rendering text.
    private StringBuffer charData[];
    private StringBuffer decoData[];
    private float textCPI = 16.67f;
    private float textLPI = 8;
    private int maxX = (int)(8.5f * textCPI + 1);
    private int maxY = (int)(11f * textLPI + 1);
    private float xFactor;
    private float yFactor;
    /** 
     * Every line except the last line on a page (which will end with 
     * pageEnding) will be terminated with this string.
     */
    private String lineEnding = "\r\n";
    /** 
     * Every page except the last one will end with this string.
     */
    private String pageEnding = "\f";
    /**
     * If true then graphics/decorations will not be rendered - text only.
     */
    private boolean suppressGraphics = false;
    private boolean firstPage = false;

    private void addStr(int row, int col, String str, boolean ischar) {
        if (debug) {
            log.debug("TXTRenderer.addStr(" + row + ", " + col
                               + ", \"" + str + "\", " + ischar + ")");
        }
        if (suppressGraphics && !ischar) {
            return;
        }
        StringBuffer sb;
        if (row < 0) {
            row = 0;
        }
        if (ischar) {
            sb = charData[row];
        } else {
            sb = decoData[row];
        }
        if (sb == null) {
            sb = new StringBuffer();
        }
        if ((col + str.length()) > maxX) {
            col = maxX - str.length();
        }
        if (col < 0) {
            col = 0;
            if (str.length() > maxX) {
                str = str.substring(0, maxX);
            }
        }
        // Pad to col
        for (int countr = sb.length(); countr < col; countr++) {
            sb.append(' ');
        }
        if (debug) {
            log.debug("TXTRenderer.addStr() sb.length()="
                               + sb.length());
        }
        for (int countr = col; countr < (col + str.length()); countr++) {
            if (countr >= sb.length()) {
                sb.append(str.charAt(countr - col));
            } else {
                if (debug) {
                    log.debug("TXTRenderer.addStr() sb.length()="
                                       + sb.length() + " countr=" + countr);
                }
                sb.setCharAt(countr, str.charAt(countr - col));
            }
        }

        if (ischar) {
            charData[row] = sb;
        } else {
            decoData[row] = sb;
        }
    }

    /** @see org.apache.fop.render.AbstractRenderer */
    public String getMimeType() {
        return MIME_TYPE;
    }

}
