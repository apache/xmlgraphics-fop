/*-- $Id$ --

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

 */
package org.apache.fop.fonts.apps;

import java.io.*;
import org.w3c.dom.*;
import org.apache.xerces.dom.*;
import org.apache.xml.serialize.*;
import org.apache.xalan.xslt.*;
import org.apache.fop.fonts.*;

/**
 * A tool which reads PFM files from Adobe Type 1 fonts and creates
 * XML font metrics file for use in FOP.
 *
 * @author  jeremias.maerki@outline.ch
 */
public class PFMReader {

    static private final String XSL_POSTPROCESS = "FontPostProcess.xsl";
    static private final String XSL_SORT        = "FontPostProcessSort.xsl";

    private boolean invokedStandalone = false;

    public PFMReader() {
    }

    /**
     * The main method for the PFM reader tool.
     * 
     * @param  args Command-line arguments: [pfm-file] {[xml-file]}
     *               If [xml-file] is not provided, then just a little preview of
     *               the PFM ist displayed.
     */
    public static void main(String[] args) {
        PFMReader app = new PFMReader();
        app.invokedStandalone = true;

        System.out.println("PFM Reader v1.0");
        System.out.println();

        if (args.length > 0) {
            PFMFile pfm = app.loadPFM(args[0]);
            if (pfm != null) {
                app.preview(pfm);

                if (args.length > 1) {
                    org.w3c.dom.Document doc = app.constructFontXML(pfm);
                    doc = app.postProcessXML(doc);
                    if (doc != null) {
                        app.writeFontXML(doc, args[1]);
                    }
                }
            }
        } else {
            System.out.println("Arguments: <source> [<target>]");
            System.out.println("Example: COM_____.pfm COM_____.xml");
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
        out.print(pfm.getLastChar()-pfm.getFirstChar());
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
            OutputFormat format = new OutputFormat(doc);     //Serialize DOM
            FileWriter out = new FileWriter(target);         //Writer will be a String
            XMLSerializer serial = new XMLSerializer(out, format);
            serial.asDOMSerializer();                        // As a DOM Serializer

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
    public org.w3c.dom.Document constructFontXML(PFMFile pfm) {
        System.out.println("Creating xml font file...");
        System.out.println();

        Document doc = new DocumentImpl();
        Element root = doc.createElement("font-metrics");
        doc.appendChild(root);

        Element el = doc.createElement("font-name");
        root.appendChild(el);
        el.appendChild(doc.createTextNode(pfm.getPostscriptName()));

        String s = pfm.getPostscriptName();
        int pos = s.indexOf("-");
        if (pos >= 0) {
            StringBuffer sb = new StringBuffer(s);
            sb.deleteCharAt(pos);
            s = sb.toString();
        }

        el = doc.createElement("class-name");
        root.appendChild(el);
        el.appendChild(doc.createTextNode(s));

        el = doc.createElement("subtype");
        root.appendChild(el);
        el.appendChild(doc.createTextNode("Type1"));

        el = doc.createElement("encoding");
        root.appendChild(el);
        el.appendChild(doc.createTextNode(pfm.getCharSetName()+"Encoding"));

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
        String[] names = {"left","bottom","right","top"};
        for (int i=0; i<4; i++) {
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

        for (short i = pfm.getFirstChar(); i < pfm.getLastChar(); i++) {
            el = doc.createElement("char");
            widths.appendChild(el);
            el.setAttribute("ansichar", "0x00" + Integer.toHexString(i).toUpperCase());
            el.setAttribute("width", new Integer(pfm.getCharWidth(i)).toString());
        }

        return doc;
    }

    /**
     * Modifies the generated font metrics file. First, it processes the
     * character mmappings, then it sorts them.
     * 
     * @param   doc The DOM document representing the font metrics file.
     * @return  A DOM document representing the processed font metrics file.
     */
    public org.w3c.dom.Document postProcessXML(org.w3c.dom.Document doc) {
        try {

            System.out.println("Postprocessing...");
            System.out.println();

            // Create the 3 objects the XSLTProcessor needs to perform the transformation.
            org.apache.xalan.xslt.XSLTInputSource xmlSource =
                new org.apache.xalan.xslt.XSLTInputSource(doc);

            InputStream xsl = this.getClass().getResourceAsStream(XSL_POSTPROCESS);
            if (xsl == null) {
                throw new Exception("Resource " + XSL_POSTPROCESS + " not found");
            }
            org.apache.xalan.xslt.XSLTInputSource xslSheet =
                new org.apache.xalan.xslt.XSLTInputSource(xsl);

            Document targetDoc = new DocumentImpl();
            org.apache.xalan.xslt.XSLTResultTarget xmlResult =
                new org.apache.xalan.xslt.XSLTResultTarget(targetDoc);

            // Use XSLTProcessorFactory to instantiate an XSLTProcessor.
            org.apache.xalan.xslt.XSLTProcessor processor =
                org.apache.xalan.xslt.XSLTProcessorFactory.getProcessor(
                    new org.apache.xalan.xpath.xdom.XercesLiaison());

            // Perform the transformation.
            processor.process(xmlSource, xslSheet, xmlResult);


            System.out.println("Sorting...");
            System.out.println();

            // Sort the whole thing
            xmlSource.setNode(targetDoc);

            xsl = this.getClass().getResourceAsStream(XSL_SORT);
            if (xsl == null) {
                throw new Exception("Resource " + XSL_SORT + " not found");
            }
            xslSheet = new org.apache.xalan.xslt.XSLTInputSource(xsl);

            org.w3c.dom.Document targetDocSorted = new DocumentImpl();
            xmlResult =
                new org.apache.xalan.xslt.XSLTResultTarget(targetDocSorted);

            // Perform the transformation (sort).
            processor.process(xmlSource, xslSheet, xmlResult);

            return targetDocSorted;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
