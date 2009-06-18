/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.pcl;

// FOP
import org.apache.fop.render.PrintRenderer;
import org.apache.fop.image.FopImage;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.pdf.PDFPathPaint;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.layout.*;
import org.apache.fop.layout.inline.*;
import org.apache.fop.image.*;

import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGDocument;


// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 * Renderer that renders areas to PCL
 * Created by Arthur E Welch III while at M&I EastPoint Technology
 * Donated by EastPoint to the Apache FOP project March 2, 2001.
 */
public class PCLRenderer extends PrintRenderer {

    /**
     * the current stream to add PCL commands to
     */
    public PCLStream currentStream;

    private int pageHeight = 7920;

    // These variables control the virtual paggination functionality.
    public int curdiv = 0;
    private int divisions = -1;
    public int paperheight = -1;    // Paper height in decipoints?
    public int orientation =
        -1;                         // -1=default/unknown, 0=portrait, 1=landscape.
    public int topmargin = -1;      // Top margin in decipoints?
    public int leftmargin = -1;     // Left margin in decipoints?
    private int fullmargin = 0;
    private final boolean debug = false;

    private int xoffset =
        -180;                       // X Offset to allow for PCL implicit 1/4" left margin.

    private java.util.Hashtable options;

    /**
     * Create the PCL renderer
     */
    public PCLRenderer() {}

    /**
     * set the PCL document's producer
     *
     * @param producer string indicating application producing PCL
     */
    public void setProducer(String producer) {}

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
            // currentStream.add("\033(9U\033(s1p" + (size / 1000) + "v0s0b25093T"); // ECMA Latin 1 Symbol Set in Times Roman???
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

    public void startRenderer(OutputStream outputStream)
    throws IOException {
        log.info("rendering areas to PCL");
        currentStream = new PCLStream(outputStream);

        // Set orientation.
        if (orientation > -1)
            currentStream.add("\033&l" + orientation + "O");
        else
            currentStream.add("\033&l0O");
        if (orientation == 1 || orientation == 3)
            xoffset = -144;
        else
            xoffset = -180;

        // Reset the margins.
        currentStream.add("\033" + "9\033&l0E");
    }

    public void stopRenderer()
    throws IOException {
    }

}
