/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.render.pdf;

import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.net.URL;

import org.apache.fop.render.pdf.fonts.*;
import org.apache.fop.pdf.PDFWArray;
import org.apache.fop.pdf.PDFCIDFont;
import org.apache.fop.configuration.ConfigurationReader;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.apps.FOPException;
import org.apache.fop.tools.URLBuilder;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;

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
    private MultiByteFont multiFont = null;
    private SingleByteFont singleFont = null;
    private Font returnFont = null;
    // private SingleByteFont singleFont = null;
    private String text = null;

    private List cidWidths = null;
    private int cidWidthIndex = 0;

    private Map currentKerning = null;

    private List bfranges = null;

    private void createFont(URL url) throws FOPException {
        XMLReader parser = ConfigurationReader.createParser();
        if (parser == null)
            throw new FOPException("Unable to create SAX parser");

        try {
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes",
                              false);
        } catch (SAXException e) {
            throw new FOPException("You need a SAX parser which supports SAX version 2",
                                   e);
        }

        parser.setContentHandler(this);

        try {
            parser.parse(new InputSource(url.openStream()));
        } catch (SAXException e) {
            throw new FOPException(e);
        } catch (IOException e) {
            throw new FOPException(e);
        }

    }

    /**
     * Sets the path to embed a font. a null value disables font embedding
     */
    public void setFontEmbedPath(URL path) {
        if (isCID)
            multiFont.embedFileName = path;
        else
            singleFont.embedFileName = path;
    }

    /**
     * Enable/disable use of kerning for the font
     */
    public void useKerning(boolean kern) {
        if (isCID)
            multiFont.useKerning = true;
        else
            singleFont.useKerning = true;
    }


    /**
     * Get the generated font object
     */
    public Font getFont() {
        return returnFont;
    }

    /**
     * Construct a FontReader object from a path to a metric.xml file
     * and read metric data
     */
    public FontReader(URL path) throws FOPException {
        createFont(path);
    }

    public void startDocument() {}

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        if (localName.equals("font-metrics")) {
            if ("TYPE0".equals(attributes.getValue("type"))) {
                multiFont = new MultiByteFont();
                returnFont = multiFont;
                isCID = true;
            } else if ("TRUETYPE".equals(attributes.getValue("type"))) {
                singleFont = new SingleByteFont();
                singleFont.subType = org.apache.fop.pdf.PDFFont.TRUETYPE;
                returnFont = singleFont;
                isCID = false;
            } else {
                singleFont = new SingleByteFont();
                singleFont.subType = org.apache.fop.pdf.PDFFont.TYPE1;
                returnFont = singleFont;
                isCID = false;
            }
        } else if ("embed".equals(localName)) {
            if (isCID) {
                /**@todo This *is* annoying... should create a common
                  interface for sing/multibytefonts...*/
                String filename = attributes.getValue("file");
                if (filename != null) {
                    try {
                        multiFont.embedFileName = URLBuilder.buildURL(
                                Configuration.getFontBaseURL(), filename);
                    } catch (java.net.MalformedURLException mfue) {
                        throw new SAXException(mfue);
                    }
                }
                multiFont.embedResourceName = attributes.getValue("class");
            } else {
                String filename = attributes.getValue("file");
                if (filename != null) {
                    try {
                        singleFont.embedFileName = URLBuilder.buildURL(
                                Configuration.getFontBaseURL(), filename);
                    } catch (java.net.MalformedURLException mfue) {
                        throw new SAXException(mfue);
                    }
                }
                singleFont.embedResourceName = attributes.getValue("class");
            }
        } else if ("cid-widths".equals(localName)) {
            cidWidthIndex = getInt(attributes.getValue("start-index"));
            cidWidths = new java.util.ArrayList();
        } else if ("kerning".equals(localName)) {
            currentKerning = new java.util.HashMap();
            if (isCID)
                multiFont.kerning.put(new Integer(attributes.getValue("kpx1")),
                                      currentKerning);
            else
                singleFont.kerning.put(new Integer(attributes.getValue("kpx1")),
                                       currentKerning);
        } else if ("bfranges".equals(localName)) {
            bfranges = new java.util.ArrayList();
        } else if ("bf".equals(localName)) {
            BFEntry entry = new BFEntry();
            entry.unicodeStart = getInt(attributes.getValue("us"));
            entry.unicodeEnd = getInt(attributes.getValue("ue"));
            entry.glyphStartIndex = getInt(attributes.getValue("gi"));
            bfranges.add(entry);
        } else if ("wx".equals(localName)) {
            cidWidths.add(new Integer(attributes.getValue("w")));
        } else if ("widths".equals(localName)) {
            singleFont.width = new int[256];
        } else if ("char".equals(localName)) {
            try {
                singleFont.width[Integer.parseInt(attributes.getValue("idx"))] =
                    Integer.parseInt(attributes.getValue("wdt"));
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
        } catch (Exception e) {}
        return ret;
    }

    public void endElement(String uri, String localName, String qName) {
        if ("font-name".equals(localName))
            if (isCID)
                multiFont.fontName = text;
            else
                singleFont.fontName = text;
        if ("ttc-name".equals(localName) && isCID)
            multiFont.ttcName = text;
        else if ("cap-height".equals(localName))
            if (isCID)
                multiFont.capHeight = getInt(text);
            else
                singleFont.capHeight = getInt(text);
        else if ("x-height".equals(localName))
            if (isCID)
                multiFont.xHeight = getInt(text);
            else
                singleFont.xHeight = getInt(text);
        else if ("ascender".equals(localName))
            if (isCID)
                multiFont.ascender = getInt(text);
            else
                singleFont.ascender = getInt(text);
        else if ("descender".equals(localName))
            if (isCID)
                multiFont.descender = getInt(text);
            else
                singleFont.descender = getInt(text);
        else if ("left".equals(localName))
            if (isCID)
                multiFont.fontBBox[0] = getInt(text);
            else
                singleFont.fontBBox[0] = getInt(text);
        else if ("bottom".equals(localName))
            if (isCID)
                multiFont.fontBBox[1] = getInt(text);
            else
                singleFont.fontBBox[1] = getInt(text);
        else if ("right".equals(localName))
            if (isCID)
                multiFont.fontBBox[2] = getInt(text);
            else
                singleFont.fontBBox[2] = getInt(text);
        else if ("first-char".equals(localName))
            singleFont.firstChar = getInt(text);
        else if ("last-char".equals(localName))
            singleFont.lastChar = getInt(text);
        else if ("top".equals(localName))
            if (isCID)
                multiFont.fontBBox[3] = getInt(text);
            else
                singleFont.fontBBox[3] = getInt(text);
        else if ("flags".equals(localName))
            if (isCID)
                multiFont.flags = getInt(text);
            else
                singleFont.flags = getInt(text);
        else if ("stemv".equals(localName))
            if (isCID)
                multiFont.stemV = getInt(text);
            else
                singleFont.stemV = getInt(text);
        else if ("italic-angle".equals(localName))
            if (isCID)
                multiFont.italicAngle = getInt(text);
            else
                singleFont.italicAngle = getInt(text);
        else if ("missing-width".equals(localName))
            if (isCID)
                multiFont.missingWidth = getInt(text);
            else
                singleFont.missingWidth = getInt(text);
        else if ("cid-type".equals(localName)) {
            if ("CIDFontType2".equals(text))
                multiFont.cidType = PDFCIDFont.CID_TYPE2;
        } else if ("default-width".equals(localName)) {
            multiFont.defaultWidth = getInt(text);
        } else if ("cid-widths".equals(localName)) {
            int[] wds = new int[cidWidths.size()];
            for (int i = 0; i <  cidWidths.size(); i++ ) {
                wds[i] = ((Integer)cidWidths.get(i)).intValue();
            }

            multiFont.warray.addEntry(cidWidthIndex, wds);
            multiFont.width = wds;

        } else if ("bfranges".equals(localName)) {
            BFEntry[] entries = new BFEntry[bfranges.size()];
            entries = (BFEntry[])bfranges.toArray(entries);
            multiFont.bfentries = entries;
        }

    }

    public void characters(char[] ch, int start, int length) {
        char c[] = new char[length];
        System.arraycopy(ch, start, c, 0, length);
        text = new String(c);
    }

}


