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

    /**
     * Set the TXT document's producer
     *
     * @param producer string indicating application producing PDF
     */
    public void setProducer(String producer) {
    }


    private void addStr(int row, int col, String str, boolean ischar) {
        if (debug) {
            getLogger().debug("TXTRenderer.addStr(" + row + ", " + col
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
            getLogger().debug("TXTRenderer.addStr() sb.length()="
                               + sb.length());
        }
        for (int countr = col; countr < (col + str.length()); countr++) {
            if (countr >= sb.length()) {
                sb.append(str.charAt(countr - col));
            } else {
                if (debug) {
                    getLogger().debug("TXTRenderer.addStr() sb.length()="
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

}
