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
 
package org.apache.fop.render.pcl;

// FOP
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.area.CTM;
import org.apache.fop.render.PrintRenderer;

// Java
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Renderer that renders areas to PCL
 * Created by Arthur E Welch III while at M&I EastPoint Technology
 * Donated by EastPoint to the Apache FOP project March 2, 2001.
 */
public class PCLRenderer extends PrintRenderer {

    /** The MIME type for PCL */
    public static final String MIME_TYPE = MimeConstants.MIME_PCL_ALT;

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
                log.error(e);
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

    /** @see org.apache.fop.render.Renderer#startRenderer(java.io.OutputStream) */
    public void startRenderer(OutputStream outputStream) throws IOException {
        log.info("rendering areas to PCL");
        log.fatal("The PCL Renderer is non-functional at this time. Please help resurrect it!");
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

    /** @see org.apache.fop.render.Renderer#stopRenderer() */
    public void stopRenderer() throws IOException {
    }

    /** @see org.apache.fop.render.AbstractRenderer */
    public String getMimeType() {
        return MIME_TYPE;
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#startVParea(CTM, Rectangle2D)
     */
    protected void startVParea(CTM ctm, Rectangle2D clippingRect) {
        // TODO Auto-generated method stub
    }

    /**
     * @see org.apache.fop.render.AbstractRenderer#endVParea()
     */
    protected void endVParea() {
        // TODO Auto-generated method stub
    }

}
