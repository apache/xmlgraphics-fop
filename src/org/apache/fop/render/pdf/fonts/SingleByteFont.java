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
package org.apache.fop.render.pdf.fonts;

//Java
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.util.Map;
import java.net.URL;

//FOP
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.layout.FontDescriptor;
import org.apache.fop.pdf.PDFStream;
import org.apache.fop.pdf.PDFT1Stream;
import org.apache.fop.pdf.PDFTTFStream;
import org.apache.fop.render.pdf.CodePointMapping;
import org.apache.fop.render.pdf.Font;
import org.apache.fop.fonts.type1.PFBParser;
import org.apache.fop.fonts.type1.PFBData;
import org.apache.fop.tools.IOUtil;


/**
 * Generic SingleByte font
 */
public class SingleByteFont extends Font implements FontDescriptor {
    public String fontName = null;
    public String encoding = "WinAnsiEncoding";
    private final CodePointMapping mapping
        = CodePointMapping.getMapping("WinAnsiEncoding");

    public int capHeight = 0;
    public int xHeight = 0;
    public int ascender = 0;
    public int descender = 0;
    public int[] fontBBox = {0, 0, 0, 0};

    public URL embedFileName = null;
    public String embedResourceName = null;
    public PDFStream embeddedFont = null;

    public int firstChar = 0;
    public int lastChar = 255;
    public int flags = 4;
    public int stemV = 0;
    public int italicAngle = 0;
    public int missingWidth = 0;

    public Map kerning = new java.util.HashMap();
    public boolean useKerning = true;

    public int width[] = null;
    public byte subType = 0;

    public final boolean hasKerningInfo() {
        return (useKerning & kerning.isEmpty());
    }

    public final Map getKerningInfo() {
        if (useKerning) {
            return kerning;
        } else {
            return new java.util.HashMap();
        }
    }

    public byte getSubType() {
        return subType;
    }

    public int getAvgWidth() {
        return -1;
    }

    public int getMinWidth() {
        return -1;
    }

    public int getMaxWidth() {
        return -1;
    }

    public int getleading() {
        return -1;
    }

    public int getStemH() {
        return 0;
    }

    public int getMissingWidth() {
        return missingWidth;
    }

    public String getCharEncoding() {
        return encoding;
    }

    public boolean isEmbeddable() {
        return (embedFileName == null && embedResourceName == null) ? false
               : true;
    }


    public PDFStream getFontFile(int i) {
        InputStream instream = null;

        // Get file first
        if (embedFileName != null) try {
            instream = embedFileName.openStream();
        } catch (Exception e) {
            MessageHandler.error("Failed to embed fontfile: "
                               + embedFileName);
        }

            // Get resource
        if (instream == null && embedResourceName != null) try {
            instream =
                new BufferedInputStream(this.getClass().getResourceAsStream(embedResourceName));
        } catch (Exception e) {
            MessageHandler.error("Failed to embed fontresource: "
                               + embedResourceName);
        }

        if (instream == null) {
            return (PDFStream)null;
        }

        // Read fontdata
        try {
            if (subType == org.apache.fop.pdf.PDFFont.TYPE1) {
                PFBParser parser = new PFBParser();
                PFBData pfb = parser.parsePFB(instream);
                embeddedFont = new PDFT1Stream(i);
                ((PDFT1Stream)embeddedFont).setData(pfb);
            } else {
                byte[] file = IOUtil.toByteArray(instream, 128000);
                embeddedFont = new PDFTTFStream(i, file.length);
                ((PDFTTFStream)embeddedFont).setData(file, file.length);
            }

            embeddedFont.addFilter("flate");
            embeddedFont.addFilter("ascii-85");
            instream.close();
        } catch (Exception e) {
            MessageHandler.error("Failed to read font data for embedded font: "+e.getMessage());
        }

        return (PDFStream)embeddedFont;
    }

    public String encoding() {
        return encoding;
    }

    public String fontName() {
        return fontName;
    }

    public int getAscender() {
        return ascender;
    }

    public int getDescender() {
        return descender;
    }

    public int getCapHeight() {
        return capHeight;
    }

    public int getAscender(int size) {
        return size * ascender;
    }

    public int getCapHeight(int size) {
        return size * capHeight;
    }

    public int getDescender(int size) {
        return size * descender;
    }

    public int getXHeight(int size) {
        return size * xHeight;
    }

    public int getFlags() {
        return flags;
    }

    public int[] getFontBBox() {
        return fontBBox;
    }

    public int getItalicAngle() {
        return italicAngle;
    }

    public int getStemV() {
        return stemV;
    }

    public int getFirstChar() {
        return 0;
        // return firstChar;
    }

    public int getLastChar() {
        return lastChar;
    }

    public int width(int i, int size) {
        return size * width[i];
    }

    public int[] getWidths(int size) {
        int[] arr = new int[width.length];
        System.arraycopy(width, 0, arr, 0, width.length - 1);
        for (int i = 0; i < arr.length; i++) {
            arr[i] *= size;
        }
        return arr;
    }

    public char mapChar(char c) {
        char d = mapping.mapChar(c);
        if(d != 0) {
            return d;
        } else {
            return '#';
        }
    }

}

