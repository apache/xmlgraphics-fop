/*
 * $Id: PCLRenderer.java,v 1.18 2003/03/07 09:46:33 jeremias Exp $
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
package org.apache.fop.render.pcl;

// FOP
import org.apache.fop.render.PrintRenderer;

// Java
import java.io.IOException;
import java.io.OutputStream;

/**
 * Renderer that renders areas to PCL
 * Created by Arthur E Welch III while at M&I EastPoint Technology
 * Donated by EastPoint to the Apache FOP project March 2, 2001.
 */
public class PCLRenderer extends PrintRenderer {

    /**
     * the current stream to add PCL commands to
     */
    protected PCLStream currentStream;

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

    private int xoffset = -180;     // X Offset to allow for PCL implicit 1/4" left margin.

    private java.util.Hashtable options;

    /**
     * Create the PCL renderer
     */
    public PCLRenderer() {
    }

    public void setFont(String name, float size) {
        int fontcode = 0;
        if (name.length() > 1 && name.charAt(0) == 'F') {
            try {
                fontcode = Integer.parseInt(name.substring(1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        switch (fontcode) {
        case 1:     // F1 = Helvetica
            // currentStream.add("\033(8U\033(s1p" + (size / 1000) + "v0s0b24580T");
            // Arial is more common among PCL5 printers than Helvetica - so use Arial

            currentStream.add("\033(0N\033(s1p" + (size / 1000)
                              + "v0s0b16602T");
            break;
        case 2:     // F2 = Helvetica Oblique

            currentStream.add("\033(0N\033(s1p" + (size / 1000)
                              + "v1s0b16602T");
            break;
        case 3:     // F3 = Helvetica Bold

            currentStream.add("\033(0N\033(s1p" + (size / 1000)
                              + "v0s3b16602T");
            break;
        case 4:     // F4 = Helvetica Bold Oblique

            currentStream.add("\033(0N\033(s1p" + (size / 1000)
                              + "v1s3b16602T");
            break;
        case 5:     // F5 = Times Roman
            // currentStream.add("\033(8U\033(s1p" + (size / 1000) + "v0s0b25093T");
            // Times New is more common among PCL5 printers than Times - so use Times New

            currentStream.add("\033(0N\033(s1p" + (size / 1000)
                              + "v0s0b16901T");
            break;
        case 6:     // F6 = Times Italic

            currentStream.add("\033(0N\033(s1p" + (size / 1000)
                              + "v1s0b16901T");
            break;
        case 7:     // F7 = Times Bold

            currentStream.add("\033(0N\033(s1p" + (size / 1000)
                              + "v0s3b16901T");
            break;
        case 8:     // F8 = Times Bold Italic

            currentStream.add("\033(0N\033(s1p" + (size / 1000)
                              + "v1s3b16901T");
            break;
        case 9:     // F9 = Courier

            currentStream.add("\033(0N\033(s0p"
                              + (120.01f / (size / 1000.00f)) + "h0s0b4099T");
            break;
        case 10:    // F10 = Courier Oblique

            currentStream.add("\033(0N\033(s0p"
                              + (120.01f / (size / 1000.00f)) + "h1s0b4099T");
            break;
        case 11:    // F11 = Courier Bold

            currentStream.add("\033(0N\033(s0p"
                              + (120.01f / (size / 1000.00f)) + "h0s3b4099T");
            break;
        case 12:    // F12 = Courier Bold Oblique

            currentStream.add("\033(0N\033(s0p"
                              + (120.01f / (size / 1000.00f)) + "h1s3b4099T");
            break;
        case 13:    // F13 = Symbol

            currentStream.add("\033(19M\033(s1p" + (size / 1000)
                              + "v0s0b16686T");
            // ECMA Latin 1 Symbol Set in Times Roman???
            // currentStream.add("\033(9U\033(s1p" + (size / 1000) + "v0s0b25093T");
            break;
        case 14:    // F14 = Zapf Dingbats

            currentStream.add("\033(14L\033(s1p" + (size / 1000)
                              + "v0s0b45101T");
            break;
        default:
            currentStream.add("\033(0N\033(s" + (size / 1000) + "V");
            break;
        }
    }

    public void startRenderer(OutputStream outputStream) throws IOException {
        getLogger().info("rendering areas to PCL");
        currentStream = new PCLStream(outputStream);

        // Set orientation.
        if (orientation > -1) {
            currentStream.add("\033&l" + orientation + "O");
        } else {
            currentStream.add("\033&l0O");
        }
        if (orientation == 1 || orientation == 3) {
            xoffset = -144;
        } else {
            xoffset = -180;
        }

        // Reset the margins.
        currentStream.add("\033" + "9\033&l0E");
    }

    public void stopRenderer() throws IOException {
    }

}
