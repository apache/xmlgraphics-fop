/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

// package com.eastpoint.chrysalis;
package org.apache.fop.render.txt;

// FOP
import org.apache.fop.render.PrintRenderer;
import org.apache.fop.render.pcl.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.layout.inline.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.pdf.PDFPathPaint;
import org.apache.fop.pdf.PDFColor;
import org.apache.fop.image.*;

import org.w3c.dom.svg.SVGSVGElement;
import org.w3c.dom.svg.SVGDocument;

// Java
import java.io.IOException;
import java.io.OutputStream;

/**
 * Renderer that renders areas to plain text
 *
 * Modified by Mark Lillywhite mark-fop@inomial.com to use the new
 * Renderer interface.
 */
public class TXTRenderer extends PrintRenderer {

    /**
     * the current stream to add Text commands to
     */
    PCLStream currentStream;

    private int pageHeight = 7920;

    // These variables control the virtual paggination functionality.
    public int curdiv = 0;
    private int divisions = -1;
    private int paperheight = -1;    // Paper height in decipoints?
    public int orientation =
        -1;                          // -1=default/unknown, 0=portrait, 1=landscape.
    public int topmargin = -1;       // Top margin in decipoints?
    public int leftmargin = -1;      // Left margin in decipoints?
    private int fullmargin = 0;
    final boolean debug = false;

    // Variables for rendering text.
    StringBuffer charData[];
    StringBuffer decoData[];
    public float textCPI = 16.67f;
    public float textLPI = 8;
    int maxX = (int)(8.5f * textCPI + 1);
    int maxY = (int)(11f * textLPI + 1);
    float xFactor;
    float yFactor;
    public String lineEnding =
        "\r\n";    // Every line except the last line on a page (which will end with pageEnding) will be terminated with this string.
    public String pageEnding =
        "\f";                        // Every page except the last one will end with this string.
    public boolean suppressGraphics =
        false;    // If true then graphics/decorations will not be rendered - text only.
    boolean firstPage = false;

    public TXTRenderer() {}

    /**
     * set the TXT document's producer
     *
     * @param producer string indicating application producing PDF
     */
    public void setProducer(String producer) {}


    void addStr(int row, int col, String str, boolean ischar) {
        if (debug)
            System.out.println("TXTRenderer.addStr(" + row + ", " + col
                               + ", \"" + str + "\", " + ischar + ")");
        if (suppressGraphics &&!ischar)
            return;
        StringBuffer sb;
        if (row < 0)
            row = 0;
        if (ischar)
            sb = charData[row];
        else
            sb = decoData[row];
        if (sb == null)
            sb = new StringBuffer();
        if ((col + str.length()) > maxX)
            col = maxX - str.length();
        if (col < 0) {
            col = 0;
            if (str.length() > maxX)
                str = str.substring(0, maxX);
        }
        // Pad to col
        for (int countr = sb.length(); countr < col; countr++)
            sb.append(' ');
        if (debug)
            System.out.println("TXTRenderer.addStr() sb.length()="
                               + sb.length());
        for (int countr = col; countr < (col + str.length()); countr++) {
            if (countr >= sb.length())
                sb.append(str.charAt(countr - col));
            else {
                if (debug)
                    System.out.println("TXTRenderer.addStr() sb.length()="
                                       + sb.length() + " countr=" + countr);
                sb.setCharAt(countr, str.charAt(countr - col));
            }
        }

        if (ischar)
            charData[row] = sb;
        else
            decoData[row] = sb;
    }

}
