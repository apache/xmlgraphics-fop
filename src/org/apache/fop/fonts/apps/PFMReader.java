/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fonts.apps;

import java.io.*;
import org.w3c.dom.*;
import org.apache.xerces.dom.*;
import org.apache.xml.serialize.*;
import org.apache.xalan.xslt.*;
import org.apache.fop.fonts.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A tool which reads PFM files from Adobe Type 1 fonts and creates
 * XML font metrics file for use in FOP.
 *
 * @author  jeremias.maerki@outline.ch
 */
public class PFMReader {
    private boolean invokedStandalone = false;

    public PFMReader() {}


    /**
     * Parse commandline arguments. put options in the HashMap and return
     * arguments in the String array
     * the arguments: -fn Perpetua,Bold -cn PerpetuaBold per.ttf Perpetua.xml
     * returns a String[] with the per.ttf and Perpetua.xml. The hash
     * will have the (key, value) pairs: (-fn, Perpetua) and (-cn, PerpetuaBold)
     */
    private static String[] parseArguments(HashMap options, String[] args) {
        ArrayList arguments = new ArrayList();
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

        return (String[])arguments.toArray(new String[0]);
    }

    private final static void displayUsage() {
        System.out.println(" java org.apache.fop.fonts.apps.PFMReader [options] metricfile.pfm xmlfile.xml\n");
        System.out.println(" where options can be:\n");
        System.out.println(" -fn <fontname>\n");
        System.out.println("     default is to use the fontname in the .ttf file, but\n"
                           + "     you can override that name to make sure that the\n");
        System.out.println("     embedded font is used (if you're embedding fonts)\n");
        System.out.println("     instead of installed fonts when viewing documents with Acrobat Reader.\n");
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

        HashMap options = new HashMap();
        String[] arguments = parseArguments(options, args);

        PFMReader app = new PFMReader();
        app.invokedStandalone = true;

        System.out.println("PFM Reader v1.1");
        System.out.println();

        if (options.get("-ef") != null)
            embFile = (String)options.get("-ef");

        if (options.get("-er") != null)
            embResource = (String)options.get("-er");

        if (options.get("-fn") != null)
            fontName = (String)options.get("-fn");

        if (options.get("-cn") != null)
            className = (String)options.get("-cn");

        if (arguments.length != 2 || options.get("-h") != null
            || options.get("-help") != null || options.get("--help") != null)
            displayUsage();
        else {
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
            System.out.println("Reading " + filename + "...");
            System.out.println();
            FileInputStream in = new FileInputStream(filename);
            PFMFile pfm = new PFMFile();
            pfm.load(in);
            return pfm;
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
        PrintStream out = System.out;

        out.print("Font: ");
        out.println(pfm.getWindowsName());
        out.print("Name: ");
        out.println(pfm.getPostscriptName());
        out.print("CharSet: ");
        out.println(pfm.getCharSetName());
        out.print("CapHeight: ");
        out.println(pfm.getCapHeight());
        out.print("XHeight: ");
        out.println(pfm.getXHeight());
        out.print("LowerCaseAscent: ");
        out.println(pfm.getLowerCaseAscent());
        out.print("LowerCaseDescent: ");
        out.println(pfm.getLowerCaseDescent());
        out.print("Having widths for ");
        out.print(pfm.getLastChar() - pfm.getFirstChar());
        out.print(" characters (");
        out.print(pfm.getFirstChar());
        out.print("-");
        out.print(pfm.getLastChar());
        out.println(").");
        out.print("for example: Char ");
        out.print(pfm.getFirstChar());
        out.print(" has a width of ");
        out.println(pfm.getCharWidth(pfm.getFirstChar()));
        out.println();
    }

    /**
     * Writes the generated DOM Document to a file.
     *
     * @param   doc The DOM Document to save.
     * @param   target The target filename for the XML file.
     */
    public void writeFontXML(org.w3c.dom.Document doc, String target) {
        System.out.println("Writing xml font file " + target + "...");
        System.out.println();

        try {
            OutputFormat format = new OutputFormat(doc);    // Serialize DOM
            FileWriter out = new FileWriter(target);    // Writer will be a String
            XMLSerializer serial = new XMLSerializer(out, format);
            serial.asDOMSerializer();                       // As a DOM Serializer

            serial.serialize(doc.getDocumentElement());
            out.close();
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
        System.out.println("Creating xml font file...");
        System.out.println();

        Document doc = new DocumentImpl();
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
        if (file != null)
            el.setAttribute("file", file);
        if (resource != null)
            el.setAttribute("class", resource);

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
        String[] names = {
            "left", "bottom", "right", "top"
        };
        for (int i = 0; i < 4; i++) {
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
        for (Iterator enum = pfm.getKerning().keySet().iterator();
                enum.hasNext(); ) {
            Integer kpx1 = (Integer)enum.next();
            el = doc.createElement("kerning");
            el.setAttribute("kpx1", kpx1.toString());
            root.appendChild(el);
            Element el2 = null;

            HashMap h2 = (HashMap)pfm.getKerning().get(kpx1);
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
            if (str.charAt(i) == '\\')
                esc.append("\\\\");
            else
                esc.append(str.charAt(i));
        }

        return esc.toString();
    }

}




