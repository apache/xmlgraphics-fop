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
package org.apache.fop.render.pdf;

//Java
import java.util.List;
import java.util.Map;
import java.io.IOException;

//SAX
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

//FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.fonts.BFEntry;
import org.apache.fop.fonts.CIDFontType;
import org.apache.fop.fonts.CustomFont;
import org.apache.fop.fonts.Font;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.MultiByteFont;
import org.apache.fop.fonts.SingleByteFont;

/**
 * Class for reading a metric.xml file and creating a font object.
 * Typical usage:
 * <pre>
 * FontReader reader = new FontReader(<path til metrics.xml>);
 * reader.setFontEmbedPath(<path to a .ttf or .pfb file or null to diable embedding>);
 * reader.useKerning(true);
 * Font f = reader.getFont();
 * </pre>
 */
public class FontReader extends DefaultHandler {
    
    private Locator locator = null;
    private boolean isCID = false;
    private CustomFont returnFont = null;
    private MultiByteFont multiFont = null;
    private SingleByteFont singleFont = null;
    private StringBuffer text = new StringBuffer();

    private List cidWidths = null;
    private int cidWidthIndex = 0;

    private Map currentKerning = null;

    private List bfranges = null;

    private void createFont(String path) throws FOPException {
        XMLReader parser = null;

        try {
            parser = javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        } catch (Exception e) {
            throw new FOPException(e);
        }
        if (parser == null) {
            throw new FOPException("Unable to create SAX parser");
        }

        try {
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes",
                              false);
        } catch (SAXException e) {
            throw new FOPException("You need a SAX parser which supports SAX version 2",
                                   e);
        }

        parser.setContentHandler(this);

        try {
            parser.parse(path);
        } catch (SAXException e) {
            throw new FOPException(e);
        } catch (IOException e) {
            throw new FOPException(e);
        }

    }

    /**
     * Sets the path to embed a font. A null value disables font embedding.
     * @param path URI for the embeddable file
     */
    public void setFontEmbedPath(String path) {
        returnFont.setEmbedFileName(path);
    }

    /**
     * Enable/disable use of kerning for the font
     * @param enabled true to enable kerning, false to disable
     */
    public void setKerningEnabled(boolean enabled) {
        returnFont.setKerningEnabled(enabled);
    }


    /**
     * Get the generated font object
     * @return the font
     */
    public Font getFont() {
        return returnFont;
    }

    /**
     * Construct a FontReader object from a path to a metric.xml file
     * and read metric data
     * @param path URI to the font metric file
     * @throws FOPException if loading the font fails
     */
    public FontReader(String path) throws FOPException {
        createFont(path);
    }

    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() {
    }

    /**
     * @see org.xml.sax.ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) {
        if (localName.equals("font-metrics")) {
            if ("TYPE0".equals(attributes.getValue("type"))) {
                multiFont = new MultiByteFont();
                returnFont = multiFont;
                isCID = true;
            } else if ("TRUETYPE".equals(attributes.getValue("type"))) {
                singleFont = new SingleByteFont();
                singleFont.setFontType(FontType.TRUETYPE);
                returnFont = singleFont;
                isCID = false;
            } else {
                singleFont = new SingleByteFont();
                singleFont.setFontType(FontType.TYPE1);
                returnFont = singleFont;
                isCID = false;
            }
        } else if ("embed".equals(localName)) {
            returnFont.setEmbedFileName(attributes.getValue("file"));
            returnFont.setEmbedResourceName(attributes.getValue("class"));
        } else if ("cid-widths".equals(localName)) {
            cidWidthIndex = getInt(attributes.getValue("start-index"));
            cidWidths = new java.util.ArrayList();
        } else if ("kerning".equals(localName)) {
            currentKerning = new java.util.HashMap();
            returnFont.putKerningEntry(new Integer(attributes.getValue("kpx1")),
                                        currentKerning);
        } else if ("bfranges".equals(localName)) {
            bfranges = new java.util.ArrayList();
        } else if ("bf".equals(localName)) {
            BFEntry entry = new BFEntry(getInt(attributes.getValue("us")),
                                        getInt(attributes.getValue("ue")),
                                        getInt(attributes.getValue("gi")));
            bfranges.add(entry);
        } else if ("wx".equals(localName)) {
            cidWidths.add(new Integer(attributes.getValue("w")));
        } else if ("widths".equals(localName)) {
            //singleFont.width = new int[256];
        } else if ("char".equals(localName)) {
            try {
                singleFont.setWidth(Integer.parseInt(attributes.getValue("idx")), 
                        Integer.parseInt(attributes.getValue("wdt")));
            } catch (NumberFormatException ne) {
                System.out.println("Malformed width in metric file: "
                                   + ne.getMessage());
            }
        } else if ("pair".equals(localName)) {
            currentKerning.put(new Integer(attributes.getValue("kpx2")),
                               new Integer(attributes.getValue("kern")));
        }
    }

    private int getInt(String str) {
        int ret = 0;
        try {
            ret = Integer.parseInt(str);
        } catch (Exception e) {
            /**@todo log this exception */
        }
        return ret;
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String uri, String localName, String qName) {
        if ("font-name".equals(localName)) {
            returnFont.setFontName(text.toString());
        } else if ("ttc-name".equals(localName) && isCID) {
            multiFont.setTTCName(text.toString());
        } else if ("cap-height".equals(localName)) {
            returnFont.setCapHeight(getInt(text.toString()));
        } else if ("x-height".equals(localName)) {
            returnFont.setXHeight(getInt(text.toString()));
        } else if ("ascender".equals(localName)) {
            returnFont.setAscender(getInt(text.toString()));
        } else if ("descender".equals(localName)) {
            returnFont.setDescender(getInt(text.toString()));
        } else if ("left".equals(localName)) {
            int[] bbox = returnFont.getFontBBox();
            bbox[0] = getInt(text.toString());
            returnFont.setFontBBox(bbox);
        } else if ("bottom".equals(localName)) {
            int[] bbox = returnFont.getFontBBox();
            bbox[1] = getInt(text.toString());
            returnFont.setFontBBox(bbox);
        } else if ("right".equals(localName)) {
            int[] bbox = returnFont.getFontBBox();
            bbox[2] = getInt(text.toString());
            returnFont.setFontBBox(bbox);
        } else if ("top".equals(localName)) {
            int[] bbox = returnFont.getFontBBox();
            bbox[3] = getInt(text.toString());
            returnFont.setFontBBox(bbox);
        } else if ("first-char".equals(localName)) {
            returnFont.setFirstChar(getInt(text.toString()));
        } else if ("last-char".equals(localName)) {
            returnFont.setLastChar(getInt(text.toString()));
        } else if ("flags".equals(localName)) {
            returnFont.setFlags(getInt(text.toString()));
        } else if ("stemv".equals(localName)) {
            returnFont.setStemV(getInt(text.toString()));
        } else if ("italic-angle".equals(localName)) {
            returnFont.setItalicAngle(getInt(text.toString()));
        } else if ("missing-width".equals(localName)) {
            returnFont.setMissingWidth(getInt(text.toString()));
        } else if ("cid-type".equals(localName)) {
            multiFont.setCIDType(CIDFontType.byName(text.toString()));
        } else if ("default-width".equals(localName)) {
            multiFont.setDefaultWidth(getInt(text.toString()));
        } else if ("cid-widths".equals(localName)) {
            int[] wds = new int[cidWidths.size()];
            int j = 0;
            for (int count = 0; count < cidWidths.size(); count++) {
                Integer i = (Integer)cidWidths.get(count);
                wds[j++] = i.intValue();
            }

            multiFont.addCIDWidthEntry(cidWidthIndex, wds);
            multiFont.setWidthArray(wds);

        } else if ("bfranges".equals(localName)) {
            multiFont.setBFEntries((BFEntry[])bfranges.toArray(new BFEntry[0]));
        }
        text.setLength(0); //Reset text buffer (see characters())
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) {
        text.append(ch, start, length);
    }

}


