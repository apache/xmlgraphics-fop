/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.fop.tools.fontlist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import org.apache.fop.Version;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.fonts.FontEventListener;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.util.GenerationHelperContentHandler;

/**
 * Command-line application to list available fonts and to optionally produce sample pages
 * with those fonts.
 */
public final class FontListMain {

    private static final int GENERATE_CONSOLE = 0;
    private static final int GENERATE_XML = 1;
    private static final int GENERATE_FO = 2;
    private static final int GENERATE_RENDERED = 3;

    private FopFactory fopFactory = FopFactory.newInstance();

    private File configFile;
    private File outputFile;
    private String configMime = MimeConstants.MIME_PDF;
    private String outputMime;
    private int mode = GENERATE_CONSOLE;
    private String singleFamilyFilter;

    private FontListMain() throws SAXException, IOException {
    }

    private void prepare() throws SAXException, IOException {
        if (this.configFile != null) {
            fopFactory.setUserConfig(this.configFile);
        }
    }

    private ContentHandler getFOPContentHandler(OutputStream out) throws FOPException {
        Fop fop = fopFactory.newFop(this.outputMime, out);
        return fop.getDefaultHandler();
    }

    private void generateXML(SortedMap fontFamilies, File outFile, String singleFamily)
                throws TransformerConfigurationException, SAXException, IOException {
        SAXTransformerFactory tFactory = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
        TransformerHandler handler;
        if (this.mode == GENERATE_XML) {
            handler = tFactory.newTransformerHandler();
        } else {
            URL url = getClass().getResource("fonts2fo.xsl");
            if (url == null) {
                throw new FileNotFoundException("Did not find resource: fonts2fo.xsl");
            }
            handler = tFactory.newTransformerHandler(new StreamSource(url.toExternalForm()));
        }

        if (singleFamily != null) {
            Transformer transformer = handler.getTransformer();
            transformer.setParameter("single-family", singleFamily);
        }

        OutputStream out = new java.io.FileOutputStream(outFile);
        out = new java.io.BufferedOutputStream(out);
        if (this.mode == GENERATE_RENDERED) {
            handler.setResult(new SAXResult(getFOPContentHandler(out)));
        } else {
            handler.setResult(new StreamResult(out));
        }
        try {
            GenerationHelperContentHandler helper = new GenerationHelperContentHandler(
                    handler, null);
            FontListSerializer serializer = new FontListSerializer();
            serializer.generateSAX(fontFamilies, singleFamily, helper);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    private void generate() throws Exception {
        prepare();

        FontEventListener listener = new FontEventListener() {

            public void fontLoadingErrorAtAutoDetection(Object source,
                    String fontURL, Exception e) {
                System.err.println("Could not load " + fontURL
                        + " (" + e.getLocalizedMessage() + ")");
            }

            public void fontSubstituted(Object source,
                    FontTriplet requested, FontTriplet effective) {
                //ignore
            }

            public void glyphNotAvailable(Object source, char ch, String fontName) {
                //ignore
            }

        };

        FontListGenerator listGenerator = new FontListGenerator();
        SortedMap fontFamilies = listGenerator.listFonts(fopFactory, configMime, listener);

        if (this.mode == GENERATE_CONSOLE) {
            writeToConsole(fontFamilies);
        } else {
            writeOutput(fontFamilies);
        }
    }

    private void writeToConsole(SortedMap fontFamilies)
            throws TransformerConfigurationException, SAXException, IOException {
        Iterator iter = fontFamilies.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            String firstFamilyName = (String)entry.getKey();
            System.out.println(firstFamilyName + ":");
            List list = (List)entry.getValue();
            Iterator fonts = list.iterator();
            while (fonts.hasNext()) {
                FontSpec f = (FontSpec)fonts.next();
                System.out.println("  " + f.getKey() + " " + f.getFamilyNames());
                Iterator triplets = f.getTriplets().iterator();
                while (triplets.hasNext()) {
                    FontTriplet triplet = (FontTriplet)triplets.next();
                    System.out.println("    " + triplet.toString());
                }
            }
        }
    }

    private void writeOutput(SortedMap fontFamilies)
            throws TransformerConfigurationException, SAXException, IOException {
        if (this.outputFile.isDirectory()) {
            System.out.println("Creating one file for each family...");
            Iterator iter = fontFamilies.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry)iter.next();
                String familyName = (String)entry.getKey();
                System.out.println("Creating output file for " + familyName + "...");
                String filename;
                switch(this.mode) {
                case GENERATE_RENDERED:
                    filename = familyName + ".pdf";
                    break;
                case GENERATE_FO:
                    filename = familyName + ".fo";
                    break;
                case GENERATE_XML:
                    filename = familyName + ".xml";
                    break;
                default:
                    throw new IllegalStateException("Unsupported mode");
                }
                File outFile = new File(this.outputFile, filename);
                generateXML(fontFamilies, outFile, familyName);
            }
        } else {
            System.out.println("Creating output file...");
            generateXML(fontFamilies, this.outputFile, this.singleFamilyFilter);
        }
        System.out.println(this.outputFile + " written.");
    }

    private static void printVersion() {
        System.out.println("Apache FOP " + Version.getVersion()
                + " - http://xmlgraphics.apache.org/fop/\n");
    }

    private static void printHelp() {
        printVersion();

        String className = FontListMain.class.getName();
        PrintStream out = System.out;
        out.println("USAGE");
        out.println("  java [vmargs] " + className
                + " [-c <config-file>] [-f <mime>] [[output-dir|output-file] [font-family]]");
        out.println();
        out.println("PARAMETERS");
        out.println("  config-file: an optional FOP configuration file");
        out.println("  mime: The MIME type of the output format for which to");
        out.println("        create the font list (defaults to application/pdf)");
        out.println("  output-dir: Creates one sample PDF per font-family");
        out.println("  output-file: writes the list as file (valid file extensions: xml, fo, pdf)");
        out.println("  font-family: filters to a single font family");
        out.println();
        out.println("EXAMPLE");
        out.println("  java [vmargs] " + className
                + " -c userconfig.xml all-fonts.pdf");
        out.println("  --> this generates a single PDF containing a sample");
        out.println("      of all configured fonts.");
        out.println("  java [vmargs] " + className
                + " -c userconfig.xml");
        out.println("  --> this prints all configured fonts to the console.");
        out.println();
    }

    private void parseArguments(String[] args) {
        if (args.length > 0) {
            int idx = 0;
            if ("--help".equals(args[idx]) || "-?".equals(args[idx]) || "-h".equals(args[idx])) {
                printHelp();
                System.exit(0);
            }
            if (idx < args.length - 1 && "-c".equals(args[idx])) {
                String filename = args[idx + 1];
                this.configFile = new File(filename);
                idx += 2;
            }
            if (idx < args.length - 1 && "-f".equals(args[idx])) {
                this.configMime = args[idx + 1];
                idx += 2;
            }
            if (idx < args.length) {
                String name = args[idx];
                this.outputFile = new File(name);
                if (this.outputFile.isDirectory()) {
                    this.mode = GENERATE_RENDERED;
                    this.outputMime = MimeConstants.MIME_PDF;
                } else if (FilenameUtils.getExtension(name).equalsIgnoreCase("pdf")) {
                    this.mode = GENERATE_RENDERED;
                    this.outputMime = MimeConstants.MIME_PDF;
                } else if (FilenameUtils.getExtension(name).equalsIgnoreCase("fo")) {
                    this.mode = GENERATE_FO;
                } else if (FilenameUtils.getExtension(name).equalsIgnoreCase("xml")) {
                    this.mode = GENERATE_XML;
                } else {
                    throw new IllegalArgumentException(
                            "Operating mode for the output file cannot be determined"
                            + " or is unsupported: " + name);
                }
                idx++;
            }
            if (idx < args.length) {
                this.singleFamilyFilter = args[idx];
            }
        } else {
            System.out.println("use --help or -? for usage information.");
        }
    }

    /**
     * The command-line interface.
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        try {
            FontListMain app = new FontListMain();
            app.parseArguments(args);
            app.generate();
        } catch (Throwable t) {
            printHelp();
            t.printStackTrace();
            System.exit(-1);
        }
    }

}
