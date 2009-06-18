/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
 
package org.apache.fop.fonts.apps;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

//Avalon
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;

//FOP
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.TTFCmapEntry;
import org.apache.fop.fonts.truetype.TTFFile;

/**
 * A tool which reads TTF files and generates
 * XML font metrics file for use in FOP.
 */
public class TTFReader extends AbstractLogEnabled {

    /**
     * Parse commandline arguments. put options in the HashMap and return
     * arguments in the String array
     * the arguments: -fn Perpetua,Bold -cn PerpetuaBold per.ttf Perpetua.xml
     * returns a String[] with the per.ttf and Perpetua.xml. The hash
     * will have the (key, value) pairs: (-fn, Perpetua) and (-cn, PerpetuaBold)
     */
    private static String[] parseArguments(Map options, String[] args) {
        List arguments = new java.util.ArrayList();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                if ((i + 1) < args.length && !args[i + 1].startsWith("-")) {
                    options.put(args[i], args[i + 1]);
                    i++;
                } else {
                    options.put(args[i], "");
                }
            } else {
                arguments.add(args[i]);
            }
        }

        return (String[])arguments.toArray(new String[0]);
    }


    private void displayUsage() {
        getLogger().info(
                " java org.apache.fop.fonts.apps.TTFReader [options] fontfile.ttf xmlfile.xml");
        getLogger().info(" where options can be:");
        getLogger().info("-d <DEBUG|INFO>");
        getLogger().info("     Set debug level (default: INFO).");
        getLogger().info("-enc ansi");
        getLogger().info("     With this option you create a WinAnsi encoded font.");
        getLogger().info("     The default is to create a CID keyed font.");
        getLogger().info("     If you're not going to use characters outside the");
        getLogger().info("     pdfencoding range (almost the same as iso-8889-1)");
        getLogger().info("     you can add this option.");
        getLogger().info("-ttcname <fontname>");
        getLogger().info("     If you're reading data from a TrueType Collection");
        getLogger().info("     (.ttc file) you must specify which font from the");
        getLogger().info("     collection you will read metrics from. If you read");
        getLogger().info("     from a .ttc file without this option, the fontnames");
        getLogger().info("      will be listed for you.");
        getLogger().info(" -fn <fontname>");
        getLogger().info("     default is to use the fontname in the .ttf file, but");
        getLogger().info("     you can override that name to make sure that the");
        getLogger().info("     embedded font is used (if you're embedding fonts)");
        getLogger().info("     instead of installed fonts when viewing documents ");
        getLogger().info("     with Acrobat Reader.");
    }


    /**
     * The main method for the TTFReader tool.
     *
     * @param  args Command-line arguments: [options] fontfile.ttf xmlfile.xml
     * where options can be:
     * -fn <fontname>
     * default is to use the fontname in the .ttf file, but you can override
     * that name to make sure that the embedded font is used instead of installed
     * fonts when viewing documents with Acrobat Reader.
     * -cn <classname>
     * default is to use the fontname
     * -ef <path to the truetype fontfile>
     * will add the possibility to embed the font. When running fop, fop will look
     * for this file to embed it
     * -er <path to truetype fontfile relative to org/apache/fop/render/pdf/fonts>
     * you can also include the fontfile in the fop.jar file when building fop.
     * You can use both -ef and -er. The file specified in -ef will be searched first,
     * then the -er file.
     */
    public static void main(String[] args) {
        String embFile = null;
        String embResource = null;
        String className = null;
        String fontName = null;
        String ttcName = null;
        boolean isCid = true;

        Map options = new java.util.HashMap();
        String[] arguments = parseArguments(options, args);

        int level = ConsoleLogger.LEVEL_INFO;
        if (options.get("-d") != null) {
            String lev = (String)options.get("-d");
            if (lev.equals("DEBUG")) {
                level = ConsoleLogger.LEVEL_DEBUG;
            } else if (lev.equals("INFO")) {
                level = ConsoleLogger.LEVEL_INFO;
            }
        }
        Logger log = new ConsoleLogger(level);

        TTFReader app = new TTFReader();
        app.enableLogging(log);

        log.info("TTF Reader v1.1.4");

        if (options.get("-enc") != null) {
            String enc = (String)options.get("-enc");
            if ("ansi".equals(enc)) {
                isCid = false;
            }
        }

        if (options.get("-ttcname") != null) {
            ttcName = (String)options.get("-ttcname");
        }

        if (options.get("-ef") != null) {
            embFile = (String)options.get("-ef");
        }

        if (options.get("-er") != null) {
            embResource = (String)options.get("-er");
        }

        if (options.get("-fn") != null) {
            fontName = (String)options.get("-fn");
        }

        if (options.get("-cn") != null) {
            className = (String)options.get("-cn");
        }

        if (arguments.length != 2 || options.get("-h") != null
            || options.get("-help") != null || options.get("--help") != null) {
            app.displayUsage();
        } else {
            try {
                TTFFile ttf = app.loadTTF(arguments[0], ttcName);
                if (ttf != null) {
                    org.w3c.dom.Document doc = app.constructFontXML(ttf,
                            fontName, className, embResource, embFile, isCid,
                            ttcName);
    
                    if (isCid) {
                        log.info("Creating CID encoded metrics");
                    } else {
                        log.info("Creating WinAnsi encoded metrics");
                    }
    
                    if (doc != null) {
                        app.writeFontXML(doc, arguments[1]);
                    }
    
                    if (ttf.isEmbeddable()) {
                        log.info("This font contains no embedding license restrictions");
                    } else {
                        log.info("** Note: This font contains license retrictions for\n"
                               + "         embedding. This font shouldn't be embedded.");
                    }
                }
            } catch (Exception e) {
                log.error("Error while building XML font metrics file", e);
                System.exit(-1);
            }
        }
    }

    /**
     * Read a TTF file and returns it as an object.
     *
     * @param  fileName The filename of the TTF file.
     * @param  fontName The name of the font
     * @return The TTF as an object, null if the font is incompatible.
     * @throws IOException In case of an I/O problem
     */
    public TTFFile loadTTF(String fileName, String fontName) throws IOException {
        TTFFile ttfFile = new TTFFile();
        setupLogger(ttfFile);
        getLogger().info("Reading " + fileName + "...");

        FontFileReader reader = new FontFileReader(fileName);
        boolean supported = ttfFile.readFont(reader, fontName);
        if (!supported) {
            return null;
        }
        return ttfFile;
    }


    /**
     * Writes the generated DOM Document to a file.
     *
     * @param   doc The DOM Document to save.
     * @param   target The target filename for the XML file.
     * @throws TransformerException if an error occurs during serialization
     */
    public void writeFontXML(org.w3c.dom.Document doc, String target) 
                throws TransformerException {
        getLogger().info("Writing xml font file " + target + "...");

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        transformer.transform(
                new javax.xml.transform.dom.DOMSource(doc),
                new javax.xml.transform.stream.StreamResult(new File(target)));
    }

    /**
     * Generates the font metrics file from the TTF/TTC file.
     *
     * @param ttf The PFM file to generate the font metrics from.
     * @param fontName Name of the font
     * @param className Class name for the font
     * @param resource path to the font as embedded resource
     * @param file path to the font as file
     * @param isCid True if the font is CID encoded
     * @param ttcName Name of the TrueType Collection
     * @return The DOM document representing the font metrics file.
     */
    public org.w3c.dom.Document constructFontXML(TTFFile ttf,
            String fontName, String className, String resource, String file,
            boolean isCid, String ttcName) {
        getLogger().info("Creating xml font file...");

        Document doc;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            doc = factory.newDocumentBuilder().newDocument();
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            getLogger().error("Can't create DOM implementation", e);
            return null;
        }
        Element root = doc.createElement("font-metrics");
        doc.appendChild(root);
        if (isCid) {
            root.setAttribute("type", "TYPE0");
        } else {
            root.setAttribute("type", "TRUETYPE");
        }

        Element el = doc.createElement("font-name");
        root.appendChild(el);

        // Note that the PostScript name usually is something like
        // "Perpetua-Bold", but the TrueType spec says that in the ttf file
        // it should be "Perpetua,Bold".

        String s = stripWhiteSpace(ttf.getPostScriptName());

        if (fontName != null) {
            el.appendChild(doc.createTextNode(stripWhiteSpace(fontName)));
        } else {
            el.appendChild(doc.createTextNode(s));
        }

        el = doc.createElement("embed");
        root.appendChild(el);
        if (file != null && ttf.isEmbeddable()) {
            el.setAttribute("file", file);
        }
        if (resource != null && ttf.isEmbeddable()) {
            el.setAttribute("class", resource);
        }

        el = doc.createElement("cap-height");
        root.appendChild(el);
        el.appendChild(doc.createTextNode(String.valueOf(ttf.getCapHeight())));

        el = doc.createElement("x-height");
        root.appendChild(el);
        el.appendChild(doc.createTextNode(String.valueOf(ttf.getXHeight())));

        el = doc.createElement("ascender");
        root.appendChild(el);
        el.appendChild(doc.createTextNode(String.valueOf(ttf.getLowerCaseAscent())));

        el = doc.createElement("descender");
        root.appendChild(el);
        el.appendChild(doc.createTextNode(String.valueOf(ttf.getLowerCaseDescent())));

        Element bbox = doc.createElement("bbox");
        root.appendChild(bbox);
        int[] bb = ttf.getFontBBox();
        final String[] names = {"left", "bottom", "right", "top"};
        for (int i = 0; i < names.length; i++) {
            el = doc.createElement(names[i]);
            bbox.appendChild(el);
            el.appendChild(doc.createTextNode(String.valueOf(bb[i])));
        }

        el = doc.createElement("flags");
        root.appendChild(el);
        el.appendChild(doc.createTextNode(String.valueOf(ttf.getFlags())));

        el = doc.createElement("stemv");
        root.appendChild(el);
        el.appendChild(doc.createTextNode(ttf.getStemV()));

        el = doc.createElement("italicangle");
        root.appendChild(el);
        el.appendChild(doc.createTextNode(ttf.getItalicAngle()));

        if (ttcName != null) {
            el = doc.createElement("ttc-name");
            root.appendChild(el);
            el.appendChild(doc.createTextNode(ttcName));
        }

        el = doc.createElement("subtype");
        root.appendChild(el);

        // Fill in extras for CID keyed fonts
        if (isCid) {
            el.appendChild(doc.createTextNode("TYPE0"));

            generateDOM4MultiByteExtras(root, ttf, isCid);
        } else {
            // Fill in extras for singlebyte fonts
            el.appendChild(doc.createTextNode("TRUETYPE"));

            generateDOM4SingleByteExtras(root, ttf, isCid);
        }

        generateDOM4Kerning(root, ttf, isCid);

        return doc;
    }

    private void generateDOM4MultiByteExtras(Element parent, TTFFile ttf, boolean isCid) {
        Element el;
        Document doc = parent.getOwnerDocument();
        
        Element mel = doc.createElement("multibyte-extras");
        parent.appendChild(mel);

        el = doc.createElement("cid-type");
        mel.appendChild(el);
        el.appendChild(doc.createTextNode("CIDFontType2"));

        el = doc.createElement("default-width");
        mel.appendChild(el);
        el.appendChild(doc.createTextNode("0"));

        el = doc.createElement("bfranges");
        mel.appendChild(el);
        Iterator e = ttf.getCMaps().listIterator();
        while (e.hasNext()) {
            TTFCmapEntry ce = (TTFCmapEntry)e.next();
            Element el2 = doc.createElement("bf");
            el.appendChild(el2);
            el2.setAttribute("us", String.valueOf(ce.getUnicodeStart()));
            el2.setAttribute("ue", String.valueOf(ce.getUnicodeEnd()));
            el2.setAttribute("gi", String.valueOf(ce.getGlyphStartIndex()));
        }

        el = doc.createElement("cid-widths");
        el.setAttribute("start-index", "0");
        mel.appendChild(el);

        int[] wx = ttf.getWidths();
        for (int i = 0; i < wx.length; i++) {
            Element wxel = doc.createElement("wx");
            wxel.setAttribute("w", String.valueOf(wx[i]));
            el.appendChild(wxel);
        }
    }

    private void generateDOM4SingleByteExtras(Element parent, TTFFile ttf, boolean isCid) {
        Element el;
        Document doc = parent.getOwnerDocument();

        Element sel = doc.createElement("singlebyte-extras");
        parent.appendChild(sel);

        el = doc.createElement("encoding");
        sel.appendChild(el);
        el.appendChild(doc.createTextNode(ttf.getCharSetName()));

        el = doc.createElement("first-char");
        sel.appendChild(el);
        el.appendChild(doc.createTextNode(String.valueOf(ttf.getFirstChar())));

        el = doc.createElement("last-char");
        sel.appendChild(el);
        el.appendChild(doc.createTextNode(String.valueOf(ttf.getLastChar())));

        Element widths = doc.createElement("widths");
        sel.appendChild(widths);

        for (short i = ttf.getFirstChar(); i <= ttf.getLastChar(); i++) {
            el = doc.createElement("char");
            widths.appendChild(el);
            el.setAttribute("idx", String.valueOf(i));
            el.setAttribute("wdt", String.valueOf(ttf.getCharWidth(i)));
        }
    }
    
    private void generateDOM4Kerning(Element parent, TTFFile ttf, boolean isCid) {
        Element el;
        Document doc = parent.getOwnerDocument();
        
        // Get kerning
        Iterator iter;
        if (isCid) {
            iter = ttf.getKerning().keySet().iterator();
        } else {
            iter = ttf.getAnsiKerning().keySet().iterator();
        }

        while (iter.hasNext()) {
            Integer kpx1 = (Integer)iter.next();

            el = doc.createElement("kerning");
            el.setAttribute("kpx1", kpx1.toString());
            parent.appendChild(el);
            Element el2 = null;

            Map h2;
            if (isCid) {
                h2 = (Map)ttf.getKerning().get(kpx1);
            } else {
                h2 = (Map)ttf.getAnsiKerning().get(kpx1);
            }

            Iterator enum2 = h2.keySet().iterator();
            while (enum2.hasNext()) {
                Integer kpx2 = (Integer)enum2.next();
                if (isCid || kpx2.intValue() < 256) {
                    el2 = doc.createElement("pair");
                    el2.setAttribute("kpx2", kpx2.toString());
                    Integer val = (Integer)h2.get(kpx2);
                    el2.setAttribute("kern", val.toString());
                    el.appendChild(el2);
                }
            }
        }
    }


    private String stripWhiteSpace(String s) {
        char[] ch = new char[s.length()];
        s.getChars(0, s.length(), ch, 0);
        StringBuffer stb = new StringBuffer();
        for (int i = 0; i < ch.length; i++) {
            if (ch[i] != ' ' 
                    && ch[i] != '\r' 
                    && ch[i] != '\n'
                    && ch[i] != '\t') {
                stb.append(ch[i]);
            }
        }

        return stb.toString();
    }

    private String escapeString(String str) {
        StringBuffer esc = new StringBuffer();

        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '\\') {
                esc.append("\\\\");
            } else {
                esc.append(str.charAt(i));
            }
        }

        return esc.toString();
    }

}

