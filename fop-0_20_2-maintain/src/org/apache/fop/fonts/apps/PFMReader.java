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
package org.apache.fop.fonts.apps;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.List;
import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.fop.fonts.type1.PFMFile;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;

/**
 * A tool which reads PFM files from Adobe Type 1 fonts and creates
 * XML font metrics file for use in FOP.
 */
public class PFMReader extends AbstractLogEnabled {

    //private boolean invokedStandalone = false;


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
                if ((i + 1) < args.length &&!args[i + 1].startsWith("-")) {
                    options.put(args[i], args[i + 1]);
                    i++;
                } else {
                    options.put(args[i], "");
                }
            } else {
                arguments.add(args[i]);
            }
        }

        String[] argStrings = new String[arguments.size()];
        arguments.toArray(argStrings);
        return argStrings;
    }

    private void displayUsage() {
        getLogger().info(" java org.apache.fop.fonts.apps.PFMReader [options] metricfile.pfm xmlfile.xml");
        getLogger().info(" where options can be:");
        getLogger().info(" -fn <fontname>");
        getLogger().info("     default is to use the fontname in the .pfm file, but");
        getLogger().info("     you can override that name to make sure that the");
        getLogger().info("     embedded font is used (if you're embedding fonts)");
        getLogger().info("     instead of installed fonts when viewing documents with Acrobat Reader.");
    }


    /**
     * The main method for the PFM reader tool.
     *
     * @param  args Command-line arguments: [options] metricfile.pfm xmlfile.xml
     * where options can be:
     * -fn <fontname>
     * default is to use the fontname in the .pfm file, but you can override
     * that name to make sure that the embedded font is used instead of installed
     * fonts when viewing documents with Acrobat Reader.
     * -cn <classname>
     * default is to use the fontname
     * -ef <path to the Type1 .pfb fontfile>
     * will add the possibility to embed the font. When running fop, fop will look
     * for this file to embed it
     * -er <path to Type1 fontfile relative to org/apache/fop/render/pdf/fonts>
     * you can also include the fontfile in the fop.jar file when building fop.
     * You can use both -ef and -er. The file specified in -ef will be searched first,
     * then the -er file.
     */
    public static void main(String[] args) {
        String embFile = null;
        String embResource = null;
        String className = null;
        String fontName = null;

        Map options = new java.util.HashMap();
        String[] arguments = parseArguments(options, args);

        PFMReader app = new PFMReader();
        Logger log;
        if (options.get("-d") != null) {
            log = new ConsoleLogger(ConsoleLogger.LEVEL_DEBUG);
        } else {
            log = new ConsoleLogger(ConsoleLogger.LEVEL_INFO);
        }
        app.enableLogging(log);

        //app.invokedStandalone = true;

        log.info("PFM Reader v1.1");
        log.info("");

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
            PFMFile pfm = app.loadPFM(arguments[0]);
            if (pfm != null) {
                app.preview(pfm);

                org.w3c.dom.Document doc = app.constructFontXML(pfm,
                        fontName, className, embResource, embFile);

                app.writeFontXML(doc, arguments[1]);
            }
        }
    }


    /**
     * Read a PFM file and returns it as an object.
     *
     * @param   filename The filename of the PFM file.
     * @return  The PFM as an object.
     */
    public PFMFile loadPFM(String filename) {
        try {
            getLogger().info("Reading " + filename + "...");
            getLogger().info("");
            InputStream in = new java.io.FileInputStream(filename);
            try {
                PFMFile pfm = new PFMFile();
                setupLogger(pfm);
                pfm.load(in);
                return pfm;
            } finally {
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Displays a preview of the PFM file on the console.
     *
     * @param   pfm The PFM file to preview.
     */
    public void preview(PFMFile pfm) {
        getLogger().info("Font: " + pfm.getWindowsName());
        getLogger().info("Name: " + pfm.getPostscriptName());
        getLogger().info("CharSet: " + pfm.getCharSetName());
        getLogger().info("CapHeight: " + pfm.getCapHeight());
        getLogger().info("XHeight: " + pfm.getXHeight());
        getLogger().info("LowerCaseAscent: " + pfm.getLowerCaseAscent());
        getLogger().info("LowerCaseDescent: " + pfm.getLowerCaseDescent());
        getLogger().info("Having widths for " + (pfm.getLastChar() - pfm.getFirstChar())
                    +" characters (" + pfm.getFirstChar()
                    + "-" + pfm.getLastChar() + ").");
        getLogger().info("for example: Char " + pfm.getFirstChar()
                    + " has a width of " + pfm.getCharWidth(pfm.getFirstChar()));
        getLogger().info("");
    }

    /**
     * Writes the generated DOM Document to a file.
     *
     * @param   doc The DOM Document to save.
     * @param   target The target filename for the XML file.
     */
    public void writeFontXML(org.w3c.dom.Document doc, String target) {
        getLogger().info("Writing xml font file " + target + "...");
        getLogger().info("");

        try {
            javax.xml.transform.TransformerFactory.newInstance()
                .newTransformer().transform(
                    new javax.xml.transform.dom.DOMSource(doc),
                    new javax.xml.transform.stream.StreamResult(new File(target)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates the font metrics file from the PFM file.
     *
     * @param   pfm The PFM file to generate the font metrics from.
     * @return  The DOM document representing the font metrics file.
     */
    public org.w3c.dom.Document constructFontXML(PFMFile pfm,
            String fontName, String className, String resource, String file) {
        getLogger().info("Creating xml font file...");
        getLogger().info("");

//        Document doc = new DocumentImpl();
        Document doc;
        try {
            doc = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        }
        catch (javax.xml.parsers.ParserConfigurationException e) {
            System.out.println("Can't create DOM implementation "+e.getMessage());
            return null;
        }
        Element root = doc.createElement("font-metrics");
        doc.appendChild(root);
        root.setAttribute("type", "TYPE1");

        Element el = doc.createElement("font-name");
        root.appendChild(el);
        el.appendChild(doc.createTextNode(pfm.getPostscriptName()));

        String s = pfm.getPostscriptName();
        int pos = s.indexOf("-");
        if (pos >= 0) {
            char sb[] = new char[s.length() - 1];
            s.getChars(0, pos, sb, 0);
            s.getChars(pos + 1, s.length(), sb, pos);
            s = new String(sb);
        }

        el = doc.createElement("embed");
        root.appendChild(el);
        if (file != null) {
            el.setAttribute("file", file);
        }
        if (resource != null) {
            el.setAttribute("class", resource);
        }

        el = doc.createElement("encoding");
        root.appendChild(el);
        el.appendChild(doc.createTextNode(pfm.getCharSetName() + "Encoding"));

        el = doc.createElement("cap-height");
        root.appendChild(el);
        Integer value = new Integer(pfm.getCapHeight());
        el.appendChild(doc.createTextNode(value.toString()));

        el = doc.createElement("x-height");
        root.appendChild(el);
        value = new Integer(pfm.getXHeight());
        el.appendChild(doc.createTextNode(value.toString()));

        el = doc.createElement("ascender");
        root.appendChild(el);
        value = new Integer(pfm.getLowerCaseAscent());
        el.appendChild(doc.createTextNode(value.toString()));

        el = doc.createElement("descender");
        root.appendChild(el);
        value = new Integer(-pfm.getLowerCaseDescent());
        el.appendChild(doc.createTextNode(value.toString()));

        Element bbox = doc.createElement("bbox");
        root.appendChild(bbox);
        int[] bb = pfm.getFontBBox();
        final String[] names = {"left", "bottom", "right", "top"};
        for (int i = 0; i < names.length; i++) {
            el = doc.createElement(names[i]);
            bbox.appendChild(el);
            value = new Integer(bb[i]);
            el.appendChild(doc.createTextNode(value.toString()));
        }

        el = doc.createElement("flags");
        root.appendChild(el);
        value = new Integer(pfm.getFlags());
        el.appendChild(doc.createTextNode(value.toString()));

        el = doc.createElement("stemv");
        root.appendChild(el);
        value = new Integer(pfm.getStemV());
        el.appendChild(doc.createTextNode(value.toString()));

        el = doc.createElement("italicangle");
        root.appendChild(el);
        value = new Integer(pfm.getItalicAngle());
        el.appendChild(doc.createTextNode(value.toString()));

        el = doc.createElement("first-char");
        root.appendChild(el);
        value = new Integer(pfm.getFirstChar());
        el.appendChild(doc.createTextNode(value.toString()));

        el = doc.createElement("last-char");
        root.appendChild(el);
        value = new Integer(pfm.getLastChar());
        el.appendChild(doc.createTextNode(value.toString()));

        Element widths = doc.createElement("widths");
        root.appendChild(widths);

        for (short i = pfm.getFirstChar(); i <= pfm.getLastChar(); i++) {
            el = doc.createElement("char");
            widths.appendChild(el);
            el.setAttribute("idx", Integer.toString(i));
            el.setAttribute("wdt",
                            new Integer(pfm.getCharWidth(i)).toString());
        }


        // Get kerning
        for (Iterator iter = pfm.getKerning().keySet().iterator(); iter.hasNext(); ) {
            Integer kpx1 = (Integer)iter.next();
            el = doc.createElement("kerning");
            el.setAttribute("kpx1", kpx1.toString());
            root.appendChild(el);
            Element el2 = null;

            Map h2 = (Map)pfm.getKerning().get(kpx1);
            for (Iterator enum2 = h2.keySet().iterator(); enum2.hasNext(); ) {
                Integer kpx2 = (Integer)enum2.next();
                el2 = doc.createElement("pair");
                el2.setAttribute("kpx2", kpx2.toString());
                Integer val = (Integer)h2.get(kpx2);
                el2.setAttribute("kern", val.toString());
                el.appendChild(el2);
            }
        }
        return doc;
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




