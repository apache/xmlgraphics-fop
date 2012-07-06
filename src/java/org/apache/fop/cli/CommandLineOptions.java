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

package org.apache.fop.cli;

// java
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.swing.UIManager;

import org.xml.sax.SAXException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.Version;
import org.apache.fop.accessibility.Accessibility;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.pdf.PDFAMode;
import org.apache.fop.pdf.PDFEncryptionManager;
import org.apache.fop.pdf.PDFEncryptionParams;
import org.apache.fop.pdf.PDFXMode;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.awt.AWTRenderer;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFSerializer;
import org.apache.fop.render.pdf.PDFEncryptionOption;
import org.apache.fop.render.print.PagesMode;
import org.apache.fop.render.print.PrintRenderer;
import org.apache.fop.render.xml.XMLRenderer;
import org.apache.fop.util.CommandLineLogger;

/**
 * Options parses the commandline arguments
 */
public class CommandLineOptions {

    /** Used to indicate that only the result of the XSL transformation should be output */
    public static final int RENDER_NONE = -1;

    /* These following constants are used to describe the input (either .FO, .XML/.XSL or
     * intermediate format)
     */

    /** (input) not set */
    public static final int NOT_SET = 0;
    /** input: fo file */
    public static final int FO_INPUT = 1;
    /** input: xml+xsl file */
    public static final int XSLT_INPUT = 2;
    /** input: Area Tree XML file */
    public static final int AREATREE_INPUT = 3;
    /** input: Intermediate Format XML file */
    public static final int IF_INPUT = 4;
    /** input: Image file */
    public static final int IMAGE_INPUT = 5;

    /* show configuration information */
    private Boolean showConfiguration = Boolean.FALSE;
    /* for area tree XML output, only down to block area level */
    private Boolean suppressLowLevelAreas = Boolean.FALSE;
    /* user configuration file */
    private File userConfigFile = null;
    /* input fo file */
    private File fofile = null;
    /* xsltfile (xslt transformation as input) */
    private File xsltfile = null;
    /* xml file (xslt transformation as input) */
    private File xmlfile = null;
    /* area tree input file */
    private File areatreefile = null;
    /* intermediate format input file */
    private File iffile = null;
    /* area tree input file */
    private File imagefile = null;
    /* output file */
    private File outfile = null;
    /* input mode */
    private int inputmode = NOT_SET;
    /* output mode */
    private String outputmode = null;
    /* true if System.in (stdin) should be used for the input file */
    private boolean useStdIn = false;
    /* true if System.out (stdout) should be used for the output file */
    private boolean useStdOut = false;
    /* true if a catalog resolver should be used for entity and uri resolution */
    private boolean useCatalogResolver = false;
    /* rendering options (for the user agent) */
    private Map renderingOptions = new java.util.HashMap();
    /* target resolution (for the user agent) */
    private int targetResolution = 0;

    private boolean strictValidation = true;
    /* control memory-conservation policy */
    private boolean conserveMemoryPolicy = false;
    /* true if a complex script features are enabled */
    private boolean useComplexScriptFeatures = true;

    private FopFactory factory;
    private FOUserAgent foUserAgent;

    private InputHandler inputHandler;

    private Log log;

    private Vector xsltParams = null;

    private String mimicRenderer = null;

    private boolean flushCache = false;

    private URI baseURI = new File(".").toURI();

    /**
     * Construct a command line option object.
     */
    public CommandLineOptions() {
        LogFactory logFactory = LogFactory.getFactory();

        // Enable the simple command line logging when no other logger is
        // defined.
        if (System.getProperty("org.apache.commons.logging.Log") == null) {
            logFactory.setAttribute("org.apache.commons.logging.Log",
                                            CommandLineLogger.class.getName());
            setLogLevel("info");
        }

        log = LogFactory.getLog("FOP");
    }

    /**
     * Parses the command line arguments.
     *
     * @param args the command line arguments.
     * @throws FOPException for general errors
     * @throws IOException if the the configuration file could not be loaded
     * @return true if the processing can continue, false to abort
     */
    public boolean parse(String[] args)
            throws FOPException, IOException {
        boolean optionsParsed = true;

        try {
            optionsParsed = parseOptions(args);
            if (optionsParsed) {
                if (showConfiguration == Boolean.TRUE) {
                    dumpConfiguration();
                }
                checkSettings();
                setUserConfig();
                if (flushCache) {
                    flushCache();
                }
                //Factory config is set up, now we can create the user agent
                foUserAgent = factory.newFOUserAgent();
                foUserAgent.getRendererOptions().putAll(renderingOptions);
                if (targetResolution != 0) {
                    foUserAgent.setTargetResolution(targetResolution);
                }
                addXSLTParameter("fop-output-format", getOutputFormat());
                addXSLTParameter("fop-version", Version.getVersion());
                foUserAgent.setConserveMemoryPolicy(conserveMemoryPolicy);
                // TODO: Handle this!!
                //if (!useComplexScriptFeatures) {
                //    foUserAgent.setComplexScriptFeaturesEnabled(false);
                //}
            } else {
                return false;
            }
        } catch (FOPException e) {
            printUsage(System.err);
            throw e;
        } catch (java.io.FileNotFoundException e) {
            printUsage(System.err);
            throw e;
        }

        inputHandler = createInputHandler();

        if (MimeConstants.MIME_FOP_AWT_PREVIEW.equals(outputmode)) {
            //set the system look&feel for the preview dialog
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Couldn't set system look & feel!");
            }

            AWTRenderer renderer = new AWTRenderer(foUserAgent, inputHandler, true, true);
            foUserAgent.setRendererOverride(renderer);
        } else if (MimeConstants.MIME_FOP_AREA_TREE.equals(outputmode)
               && mimicRenderer != null) {
            // render from FO to Intermediate Format
            Renderer targetRenderer = foUserAgent.getRendererFactory().createRenderer(
                   foUserAgent, mimicRenderer);
            XMLRenderer xmlRenderer = new XMLRenderer(foUserAgent);

            //Tell the XMLRenderer to mimic the target renderer
            xmlRenderer.mimicRenderer(targetRenderer);

            //Make sure the prepared XMLRenderer is used
            foUserAgent.setRendererOverride(xmlRenderer);
        } else if (MimeConstants.MIME_FOP_IF.equals(outputmode)
                && mimicRenderer != null) {
            // render from FO to Intermediate Format
            IFSerializer serializer = new IFSerializer(new IFContext(foUserAgent));
            IFDocumentHandler targetHandler
                = foUserAgent.getRendererFactory().createDocumentHandler(
                        foUserAgent, mimicRenderer);
            serializer.mimicDocumentHandler(targetHandler);

            //Make sure the prepared serializer is used
            foUserAgent.setDocumentHandlerOverride(serializer);
        }
        return true;
    }

    /**
     * @return the InputHandler instance defined by the command-line options.
     */
    public InputHandler getInputHandler() {
        return inputHandler;
    }

    /**
     * Get the logger.
     * @return the logger
     */
    public Log getLogger() {
        return log;
    }

    private void addXSLTParameter(String name, String value) {
        if (xsltParams == null) {
            xsltParams = new Vector();
        }
        xsltParams.addElement(name);
        xsltParams.addElement(value);
    }

    /**
     * Parses the command line arguments.
     *
     * @return true if processing can continue, false if it should stop (nothing to do)
     * @exception FOPException if there was an error in the format of the options
     */
    private boolean parseOptions(String[] args) throws FOPException {
        // do not throw an exception for no args
        if (args.length == 0) {
            printVersion();
            printUsage(System.out);
            return false;
        }
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-x")
                       || args[i].equals("--dump-config")) {
                showConfiguration = Boolean.TRUE;
            } else if (args[i].equals("-c")) {
                i = i + parseConfigurationOption(args, i);
            } else if (args[i].equals("-l")) {
                i = i + parseLanguageOption(args, i);
            } else if (args[i].equals("-s")) {
                suppressLowLevelAreas = Boolean.TRUE;
            } else if (args[i].equals("-d")) {
                setLogOption("debug", "debug");
            } else if (args[i].equals("-r")) {
                strictValidation = false;
            } else if (args[i].equals("-conserve")) {
                conserveMemoryPolicy = true;
            } else if (args[i].equals("-flush")) {
                flushCache = true;
            } else if (args[i].equals("-cache")) {
                parseCacheOption(args, i);
            } else if (args[i].equals("-dpi")) {
                i = i + parseResolution(args, i);
            } else if (args[i].equals("-q") || args[i].equals("--quiet")) {
                setLogOption("quiet", "error");
            } else if (args[i].equals("-fo")) {
                i = i + parseFOInputOption(args, i);
            } else if (args[i].equals("-xsl")) {
                i = i + parseXSLInputOption(args, i);
            } else if (args[i].equals("-xml")) {
                i = i + parseXMLInputOption(args, i);
            } else if (args[i].equals("-atin")) {
                i = i + parseAreaTreeInputOption(args, i);
            } else if (args[i].equals("-ifin")) {
                i = i + parseIFInputOption(args, i);
            } else if (args[i].equals("-imagein")) {
                i = i + parseImageInputOption(args, i);
            } else if (args[i].equals("-awt")) {
                i = i + parseAWTOutputOption(args, i);
            } else if (args[i].equals("-pdf")) {
                i = i + parsePDFOutputOption(args, i, null);
            } else if (args[i].equals("-pdfa1b")) {
                i = i + parsePDFOutputOption(args, i, "PDF/A-1b");
            } else if (args[i].equals("-mif")) {
                i = i + parseMIFOutputOption(args, i);
            } else if (args[i].equals("-rtf")) {
                i = i + parseRTFOutputOption(args, i);
            } else if (args[i].equals("-tiff")) {
                i = i + parseTIFFOutputOption(args, i);
            } else if (args[i].equals("-png")) {
                i = i + parsePNGOutputOption(args, i);
            } else if (args[i].equals("-print")) {
                // show print help
                if (i + 1 < args.length) {
                    if (args[i + 1].equals("help")) {
                        printUsagePrintOutput();
                        return false;
                    }
                }
                i = i + parsePrintOutputOption(args, i);
            } else if (args[i].equals("-copies")) {
                i = i + parseCopiesOption(args, i);
            } else if (args[i].equals("-pcl")) {
                i = i + parsePCLOutputOption(args, i);
            } else if (args[i].equals("-ps")) {
                i = i + parsePostscriptOutputOption(args, i);
            } else if (args[i].equals("-txt")) {
                i = i + parseTextOutputOption(args, i);
            } else if (args[i].equals("-svg")) {
                i = i + parseSVGOutputOption(args, i);
            } else if (args[i].equals("-afp")) {
                i = i + parseAFPOutputOption(args, i);
            } else if (args[i].equals("-foout")) {
                i = i + parseFOOutputOption(args, i);
            } else if (args[i].equals("-out")) {
                i = i + parseCustomOutputOption(args, i);
            } else if (args[i].equals("-at")) {
                i = i + parseAreaTreeOption(args, i);
            } else if (args[i].equals("-if")) {
                i = i + parseIntermediateFormatOption(args, i);
            } else if (args[i].equals("-a")) {
                this.renderingOptions.put(Accessibility.ACCESSIBILITY, Boolean.TRUE);
            } else if (args[i].equals("-v")) {
                /* verbose mode although users may expect version; currently just print the version */
                printVersion();
                if (args.length == 1) {
                    return false;
                }
            } else if (args[i].equals("-param")) {
                  if (i + 2 < args.length) {
                      String name = args[++i];
                      String expression = args[++i];
                      addXSLTParameter(name, expression);
                  } else {
                      throw new FOPException("invalid param usage: use -param <name> <value>");
                  }
            } else if (args[i].equals("-catalog")) {
                useCatalogResolver = true;
            } else if (args[i].equals("-o")) {
                i = i + parsePDFOwnerPassword(args, i);
            } else if (args[i].equals("-u")) {
                i = i + parsePDFUserPassword(args, i);
            } else if (args[i].equals("-pdfprofile")) {
                i = i + parsePDFProfile(args, i);
            } else if (args[i].equals("-noprint")) {
                getPDFEncryptionParams().setAllowPrint(false);
            } else if (args[i].equals("-nocopy")) {
                getPDFEncryptionParams().setAllowCopyContent(false);
            } else if (args[i].equals("-noedit")) {
                getPDFEncryptionParams().setAllowEditContent(false);
            } else if (args[i].equals("-noannotations")) {
                getPDFEncryptionParams().setAllowEditAnnotations(false);
            } else if (args[i].equals("-nocs")) {
                useComplexScriptFeatures = false;
            } else if (args[i].equals("-nofillinforms")) {
                getPDFEncryptionParams().setAllowFillInForms(false);
            } else if (args[i].equals("-noaccesscontent")) {
                getPDFEncryptionParams().setAllowAccessContent(false);
            } else if (args[i].equals("-noassembledoc")) {
                getPDFEncryptionParams().setAllowAssembleDocument(false);
            } else if (args[i].equals("-noprinthq")) {
                getPDFEncryptionParams().setAllowPrintHq(false);
            } else if (args[i].equals("-version")) {
                printVersion();
                return false;
            } else if (!isOption(args[i])) {
                i = i + parseUnknownOption(args, i);
            } else {
                printUsage(System.err);
                System.exit(1);
            }
        }
        return true;
    }    // end parseOptions

    private int parseCacheOption(String[] args, int i) throws FOPException {
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("if you use '-cache', you must specify "
              + "the name of the font cache file");
        } else {
            factory.getFontManager().setCacheFile(new File(args[i + 1]));
            return 1;
        }
    }

    private int parseConfigurationOption(String[] args, int i) throws FOPException {
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("if you use '-c', you must specify "
              + "the name of the configuration file");
        } else {
            userConfigFile = new File(args[i + 1]);
            return 1;
        }
    }

    private int parseLanguageOption(String[] args, int i) throws FOPException {
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("if you use '-l', you must specify a language");
        } else {
            Locale.setDefault(new Locale(args[i + 1], ""));
            return 1;
        }
    }

    private int parseResolution(String[] args, int i) throws FOPException {
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException(
                    "if you use '-dpi', you must specify a resolution (dots per inch)");
        } else {
            this.targetResolution = Integer.parseInt(args[i + 1]);
            return 1;
        }
    }

    private int parseFOInputOption(String[] args, int i) throws FOPException {
        setInputFormat(FO_INPUT);
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("you must specify the fo file for the '-fo' option");
        } else {
            String filename = args[i + 1];
            if (isSystemInOutFile(filename)) {
                this.useStdIn = true;
            } else {
                fofile = new File(filename);
                baseURI = fofile.toURI();
            }
            return 1;
        }
    }

    private int parseXSLInputOption(String[] args, int i) throws FOPException {
        setInputFormat(XSLT_INPUT);
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("you must specify the stylesheet "
                            + "file for the '-xsl' option");
        } else {
            xsltfile = new File(args[i + 1]);
            return 1;
        }
    }

    private int parseXMLInputOption(String[] args, int i) throws FOPException {
        setInputFormat(XSLT_INPUT);
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("you must specify the input file "
                            + "for the '-xml' option");
        } else {
            String filename = args[i + 1];
            if (isSystemInOutFile(filename)) {
                this.useStdIn = true;
            } else {
                xmlfile = new File(filename);
                baseURI = xmlfile.toURI();
            }
            return 1;
        }
    }

    private int parseAWTOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(MimeConstants.MIME_FOP_AWT_PREVIEW);
        return 0;
    }

    private int parsePDFOutputOption(String[] args, int i, String pdfAMode) throws FOPException {
        setOutputMode(MimeConstants.MIME_PDF);
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("you must specify the PDF output file");
        } else {
            setOutputFile(args[i + 1]);
            if (pdfAMode != null) {
                if (renderingOptions.get("pdf-a-mode") != null) {
                    throw new FOPException("PDF/A mode already set");
                }
                renderingOptions.put("pdf-a-mode", pdfAMode);
            }
            return 1;
        }
    }

    private void setOutputFile(String filename) {
        if (isSystemInOutFile(filename)) {
            this.useStdOut = true;
        } else {
            outfile = new File(filename);
        }
    }

    /**
     * Checks whether the given argument is the next option or the specification of
     * stdin/stdout.
     *
     * TODO this is very ad-hoc and should be better handled. Consider the adoption of
     * Apache Commons CLI.
     *
     * @param arg an argument
     * @return true if the argument is an option ("-something"), false otherwise
     */
    private boolean isOption(String arg) {
        return arg.length() > 1 && arg.startsWith("-");
    }

    private boolean isSystemInOutFile(String filename) {
        return "-".equals(filename);
    }

    private int parseMIFOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(MimeConstants.MIME_MIF);
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("you must specify the MIF output file");
        } else {
            setOutputFile(args[i + 1]);
            return 1;
        }
    }

    private int parseRTFOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(MimeConstants.MIME_RTF);
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("you must specify the RTF output file");
        } else {
            setOutputFile(args[i + 1]);
            return 1;
        }
    }

    private int parseTIFFOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(MimeConstants.MIME_TIFF);
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("you must specify the TIFF output file");
        } else {
            setOutputFile(args[i + 1]);
            return 1;
        }
    }

    private int parsePNGOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(MimeConstants.MIME_PNG);
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("you must specify the PNG output file");
        } else {
            setOutputFile(args[i + 1]);
            return 1;
        }
    }

    private int parsePrintOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(MimeConstants.MIME_FOP_PRINT);
        if ((i + 1 < args.length)
                && (args[i + 1].charAt(0) != '-')) {
            String arg = args[i + 1];
            String[] parts = arg.split(",");
            for (int j = 0; j < parts.length; j++) {
                String s = parts[j];
                if (s.matches("\\d+")) {
                    renderingOptions.put(PrintRenderer.START_PAGE, new Integer(s));
                } else if (s.matches("\\d+-\\d+")) {
                    String[] startend = s.split("-");
                    renderingOptions.put(PrintRenderer.START_PAGE, new Integer(startend[0]));
                    renderingOptions.put(PrintRenderer.END_PAGE, new Integer(startend[1]));
                } else {
                    PagesMode mode = PagesMode.byName(s);
                    renderingOptions.put(PrintRenderer.PAGES_MODE, mode);
                }
            }
            return 1;
        } else {
            return 0;
        }
    }

    private int parseCopiesOption(String[] args, int i) throws FOPException {
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("you must specify the number of copies");
        } else {
            renderingOptions.put(PrintRenderer.COPIES, new Integer(args[i + 1]));
            return 1;
        }
    }

    private int parsePCLOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(MimeConstants.MIME_PCL);
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("you must specify the PDF output file");
        } else {
            setOutputFile(args[i + 1]);
            return 1;
        }
    }

    private int parsePostscriptOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(MimeConstants.MIME_POSTSCRIPT);
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("you must specify the PostScript output file");
        } else {
            setOutputFile(args[i + 1]);
            return 1;
        }
    }

    private int parseTextOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(MimeConstants.MIME_PLAIN_TEXT);
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("you must specify the text output file");
        } else {
            setOutputFile(args[i + 1]);
            return 1;
        }
    }

    private int parseSVGOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(MimeConstants.MIME_SVG);
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("you must specify the SVG output file");
        } else {
            setOutputFile(args[i + 1]);
            return 1;
        }
    }

    private int parseAFPOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(MimeConstants.MIME_AFP);
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("you must specify the AFP output file");
        } else {
            setOutputFile(args[i + 1]);
            return 1;
        }
    }

    private int parseFOOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(MimeConstants.MIME_XSL_FO);
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("you must specify the FO output file");
        } else {
            setOutputFile(args[i + 1]);
            return 1;
        }
    }

    private int parseCustomOutputOption(String[] args, int i) throws FOPException {
        String mime = null;
        if ((i + 1 < args.length)
                || (args[i + 1].charAt(0) != '-')) {
            mime = args[i + 1];
            if ("list".equals(mime)) {
                String[] mimes = factory.getRendererFactory().listSupportedMimeTypes();
                System.out.println("Supported MIME types:");
                for (int j = 0; j < mimes.length; j++) {
                    System.out.println("  " + mimes[j]);
                }
                System.exit(0);
            }
        }
        if ((i + 2 >= args.length)
                || (isOption(args[i + 1]))
                || (isOption(args[i + 2]))) {
            throw new FOPException("you must specify the output format and the output file");
        } else {
            setOutputMode(mime);
            setOutputFile(args[i + 2]);
            return 2;
        }
    }

    private int parseUnknownOption(String[] args, int i) throws FOPException {
        if (inputmode == NOT_SET) {
            inputmode = FO_INPUT;
            String filename = args[i];
            if (isSystemInOutFile(filename)) {
                this.useStdIn = true;
            } else {
                fofile = new File(filename);
            }
        } else if (outputmode == null) {
            outputmode = MimeConstants.MIME_PDF;
            setOutputFile(args[i]);
        } else {
            throw new FOPException("Don't know what to do with "
                           + args[i]);
        }
        return 0;
    }

    private int parseAreaTreeOption(String[] args, int i) throws FOPException {
        setOutputMode(MimeConstants.MIME_FOP_AREA_TREE);
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("you must specify the area-tree output file");
        } else if ((i + 2 == args.length)
                || (isOption(args[i + 2]))) {
            // only output file is specified
            setOutputFile(args[i + 1]);
            return 1;
        } else {
            // mimic format and output file have been specified
            mimicRenderer = args[i + 1];
            setOutputFile(args[i + 2]);
            return 2;
        }
    }

    private int parseIntermediateFormatOption(String[] args, int i) throws FOPException {
        setOutputMode(MimeConstants.MIME_FOP_IF);
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("you must specify the intermediate format output file");
        } else if ((i + 2 == args.length)
                || (args[i + 2].charAt(0) == '-')) {
            // only output file is specified
            setOutputFile(args[i + 1]);
            return 1;
        } else {
            // mimic format and output file have been specified
            mimicRenderer = args[i + 1];
            setOutputFile(args[i + 2]);
            return 2;
        }
    }

    private int parseAreaTreeInputOption(String[] args, int i) throws FOPException {
        setInputFormat(AREATREE_INPUT);
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("you must specify the Area Tree file for the '-atin' option");
        } else {
            String filename = args[i + 1];
            if (isSystemInOutFile(filename)) {
                this.useStdIn = true;
            } else {
                areatreefile = new File(filename);
                baseURI = areatreefile.toURI();
            }
            return 1;
        }
    }

    private int parseIFInputOption(String[] args, int i) throws FOPException {
        setInputFormat(IF_INPUT);
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("you must specify the intermediate file for the '-ifin' option");
        } else {
            String filename = args[i + 1];
            if (isSystemInOutFile(filename)) {
                this.useStdIn = true;
            } else {
                iffile = new File(filename);
                baseURI = iffile.toURI();
            }
            return 1;
        }
    }

    private int parseImageInputOption(String[] args, int i) throws FOPException {
        setInputFormat(IMAGE_INPUT);
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("you must specify the image file for the '-imagein' option");
        } else {
            String filename = args[i + 1];
            if (isSystemInOutFile(filename)) {
                this.useStdIn = true;
            } else {
                imagefile = new File(filename);
                baseURI = imagefile.toURI();
            }
            return 1;
        }
    }

    private PDFEncryptionParams getPDFEncryptionParams() throws FOPException {
        PDFEncryptionParams params = (PDFEncryptionParams) renderingOptions.get(PDFEncryptionOption.ENCRYPTION_PARAMS);
        if (params == null) {
            if (!PDFEncryptionManager.checkAvailableAlgorithms()) {
                throw new FOPException("PDF encryption requested but it is not available."
                        + " Please make sure MD5 and RC4 algorithms are available.");
            }
            params = new PDFEncryptionParams();
            renderingOptions.put(PDFEncryptionOption.ENCRYPTION_PARAMS, params);
        }
        return params;
    }

    private int parsePDFOwnerPassword(String[] args, int i) throws FOPException {
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            getPDFEncryptionParams().setOwnerPassword("");
            return 0;
        } else {
            getPDFEncryptionParams().setOwnerPassword(args[i + 1]);
            return 1;
        }
    }

    private int parsePDFUserPassword(String[] args, int i) throws FOPException {
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            getPDFEncryptionParams().setUserPassword("");
            return 0;
        } else {
            getPDFEncryptionParams().setUserPassword(args[i + 1]);
            return 1;
        }
    }

    private int parsePDFProfile(String[] args, int i) throws FOPException {
        if ((i + 1 == args.length)
                || (isOption(args[i + 1]))) {
            throw new FOPException("You must specify a PDF profile");
        } else {
            String profile = args[i + 1];
            PDFAMode pdfAMode = PDFAMode.getValueOf(profile);
            if (pdfAMode != null && pdfAMode != PDFAMode.DISABLED) {
                if (renderingOptions.get("pdf-a-mode") != null) {
                    throw new FOPException("PDF/A mode already set");
                }
                renderingOptions.put("pdf-a-mode", pdfAMode.getName());
                return 1;
            } else {
                PDFXMode pdfXMode = PDFXMode.getValueOf(profile);
                if (pdfXMode != null && pdfXMode != PDFXMode.DISABLED) {
                    if (renderingOptions.get("pdf-x-mode") != null) {
                        throw new FOPException("PDF/X mode already set");
                    }
                    renderingOptions.put("pdf-x-mode", pdfXMode.getName());
                    return 1;
                }
            }
            throw new FOPException("Unsupported PDF profile: " + profile);
        }
    }

    private void setOutputMode(String mime) throws FOPException {
        if (outputmode == null) {
            outputmode = mime;
        } else {
            throw new FOPException("you can only set one output method");
        }
    }

    private void setLogOption (String option, String level) {
        if (log instanceof CommandLineLogger
            || System.getProperty("org.apache.commons.logging.Log") == null) {
            setLogLevel(level);
        } else if (log != null) {
            log.warn("The option " + option + " can only be used");
            log.warn("with FOP's command line logger,");
            log.warn("which is the default on the command line.");
            log.warn("Configure other loggers using Java system properties.");
        }
    }

    private void setLogLevel(String level) {
        // Set the level for future loggers.
        LogFactory.getFactory().setAttribute("level", level);
        if (log instanceof CommandLineLogger) {
            // Set the level for the logger created already.
            ((CommandLineLogger) log).setLogLevel(level);
        }
    }

    private void setInputFormat(int format) throws FOPException {
        if (inputmode == NOT_SET || inputmode == format) {
            inputmode = format;
        } else {
            throw new FOPException("Only one input mode can be specified!");
        }
    }

    /**
     * checks whether all necessary information has been given in a consistent way
     */
    private void checkSettings() throws FOPException, FileNotFoundException {
        if (inputmode == NOT_SET) {
            throw new FOPException("No input file specified");
        }

        if (outputmode == null) {
            throw new FOPException("No output file specified");
        }

        if ((outputmode.equals(MimeConstants.MIME_FOP_AWT_PREVIEW)
                || outputmode.equals(MimeConstants.MIME_FOP_PRINT))
                    && outfile != null) {
            throw new FOPException("Output file may not be specified "
                    + "for AWT or PRINT output");
        }

        if (inputmode == XSLT_INPUT) {
            // check whether xml *and* xslt file have been set
            if (xmlfile == null && !this.useStdIn) {
                throw new FOPException("XML file must be specified for the transform mode");
            }
            if (xsltfile == null) {
                throw new FOPException("XSLT file must be specified for the transform mode");
            }

            // warning if fofile has been set in xslt mode
            if (fofile != null) {
                log.warn("Can't use fo file with transform mode! Ignoring.\n"
                                       + "Your input is " + "\n xmlfile: "
                                       + xmlfile.getAbsolutePath()
                                       + "\nxsltfile: "
                                       + xsltfile.getAbsolutePath()
                                       + "\n  fofile: "
                                       + fofile.getAbsolutePath());
            }
            if (xmlfile != null && !xmlfile.exists()) {
                throw new FileNotFoundException("Error: xml file "
                                                + xmlfile.getAbsolutePath()
                                                + " not found ");
            }
            if (!xsltfile.exists()) {
                throw new FileNotFoundException("Error: xsl file "
                                                + xsltfile.getAbsolutePath()
                                                + " not found ");
            }

        } else if (inputmode == FO_INPUT) {
            if (outputmode.equals(MimeConstants.MIME_XSL_FO)) {
                throw new FOPException(
                        "FO output mode is only available if you use -xml and -xsl");
            }
            if (fofile != null && !fofile.exists()) {
                throw new FileNotFoundException("Error: fo file "
                                                + fofile.getAbsolutePath()
                                                + " not found ");
            }
        } else if (inputmode == AREATREE_INPUT) {
            if (outputmode.equals(MimeConstants.MIME_XSL_FO)) {
                throw new FOPException(
                        "FO output mode is only available if you use -xml and -xsl");
            } else if (outputmode.equals(MimeConstants.MIME_FOP_AREA_TREE)) {
                throw new FOPException(
                        "Area Tree Output is not available if Area Tree is used as input!");
            }
            if (areatreefile != null && !areatreefile.exists()) {
                throw new FileNotFoundException("Error: area tree file "
                                              + areatreefile.getAbsolutePath()
                                              + " not found ");
            }
        } else if (inputmode == IF_INPUT) {
            if (outputmode.equals(MimeConstants.MIME_XSL_FO)) {
                throw new FOPException(
                        "FO output mode is only available if you use -xml and -xsl");
            } else if (outputmode.equals(MimeConstants.MIME_FOP_AREA_TREE)) {
                throw new FOPException(
                    "Area Tree Output is not available if Intermediate Format"
                    + " is used as input!");
            } else if (outputmode.equals(MimeConstants.MIME_FOP_IF)) {
                throw new FOPException(
                    "Intermediate Output is not available if Intermediate Format"
                    + " is used as input!");
            }
            if (iffile != null && !iffile.exists()) {
                throw new FileNotFoundException("Error: intermediate format file "
                                              + iffile.getAbsolutePath()
                                              + " not found ");
            }
        } else if (inputmode == IMAGE_INPUT) {
            if (outputmode.equals(MimeConstants.MIME_XSL_FO)) {
                throw new FOPException(
                        "FO output mode is only available if you use -xml and -xsl");
            }
            if (imagefile != null && !imagefile.exists()) {
                throw new FileNotFoundException("Error: image file "
                                              + imagefile.getAbsolutePath()
                                              + " not found ");
            }
        }
    }    // end checkSettings

    /**
     * Sets the user configuration.
     * @throws FOPException if creating the user configuration fails
     * @throws IOException
     */
    private void setUserConfig() throws FOPException, IOException {
        FopFactoryBuilder fopFactoryBuilder;
        if (userConfigFile == null) {
            fopFactoryBuilder = new FopFactoryBuilder(baseURI);
            fopFactoryBuilder.setStrictFOValidation(strictValidation);
            fopFactoryBuilder.setTargetResolution(targetResolution);
            fopFactoryBuilder.setComplexScriptFeatures(useComplexScriptFeatures);
        } else {
            try {
                fopFactoryBuilder = new FopConfParser(userConfigFile).getFopFactoryBuilder();
            } catch (SAXException e) {
                throw new FOPException(e);
            }
            if (!strictValidation) {
                fopFactoryBuilder.setStrictFOValidation(strictValidation);
            }
            if (useComplexScriptFeatures) {
                fopFactoryBuilder.setComplexScriptFeatures(useComplexScriptFeatures);
            }
        }
        factory = fopFactoryBuilder.build();
     }

    /**
     * @return the chosen output format (MIME type)
     * @throws FOPException for invalid output formats
     */
    protected String getOutputFormat() throws FOPException {
        if (outputmode == null) {
            throw new FOPException("Renderer has not been set!");
        }
        if (outputmode.equals(MimeConstants.MIME_FOP_AREA_TREE)) {
            renderingOptions.put("fineDetail", isCoarseAreaXml());
        }
        return outputmode;
    }

    /**
     * Create an InputHandler object based on command-line parameters
     * @return a new InputHandler instance
     * @throws IllegalArgumentException if invalid/missing parameters
     */
    private InputHandler createInputHandler() {
        switch (inputmode) {
            case FO_INPUT:
                return new InputHandler(fofile);
            case AREATREE_INPUT:
                return new AreaTreeInputHandler(areatreefile);
            case IF_INPUT:
                return new IFInputHandler(iffile);
            case XSLT_INPUT:
                InputHandler handler = new InputHandler(xmlfile, xsltfile, xsltParams);
                if (useCatalogResolver) {
                    handler.createCatalogResolver(foUserAgent);
                }
                return handler;
            case IMAGE_INPUT:
                return new ImageInputHandler(imagefile, xsltfile, xsltParams);
            default:
                throw new IllegalArgumentException("Error creating InputHandler object.");
        }
    }

    /**
     * Get the FOUserAgent for this Command-Line run
     * @return FOUserAgent instance
     */
    protected FOUserAgent getFOUserAgent() {
        return foUserAgent;
    }

    /**
     * Returns the XSL-FO file if set.
     * @return the XSL-FO file, null if not set
     */
    public File getFOFile() {
        return fofile;
    }

    /**
     * Returns the input XML file if set.
     * @return the input XML file, null if not set
     */
    public File getXMLFile() {
        return xmlfile;
    }

    /**
     * Returns the stylesheet to be used for transformation to XSL-FO.
     * @return stylesheet
     */
    public File getXSLFile() {
        return xsltfile;
    }

    /**
     * Returns the output file
     * @return the output file
     */
    public File getOutputFile() {
        return outfile;
    }

    /**
     * Returns the user configuration file to be used.
     * @return the userconfig.xml file
     */
    public File getUserConfigFile() {
        return userConfigFile;
    }

    /**
     * Indicates whether the XML renderer should generate coarse area XML
     * @return true if coarse area XML is desired
     */
    public Boolean isCoarseAreaXml() {
        return suppressLowLevelAreas;
    }

    /**
     * Indicates whether input comes from standard input (stdin).
     * @return true if input comes from standard input (stdin)
     */
    public boolean isInputFromStdIn() {
        return this.useStdIn;
    }

    /**
     * Indicates whether output is sent to standard output (stdout).
     * @return true if output is sent to standard output (stdout)
     */
    public boolean isOutputToStdOut() {
        return this.useStdOut;
    }

    /**
     * Returns the input file.
     * @return either the fofile or the xmlfile
     */
    public File getInputFile() {
        switch (inputmode) {
        case FO_INPUT:
            return fofile;
        case XSLT_INPUT:
            return xmlfile;
        default:
            return fofile;
        }
    }

    private static void printVersion() {
        System.out.println("FOP Version " + Version.getVersion());
    }

    /**
     * Shows the command line syntax including a summary of all available options and some
     * examples.
     *
     * @param out the stream to which the message must be printed
     */
    public static void printUsage(PrintStream out) {
        out.println(
              "\nUSAGE\nfop [options] [-fo|-xml] infile [-xsl file] "
                    + "[-awt|-pdf|-mif|-rtf|-tiff|-png|-pcl|-ps|-txt|-at [mime]|-print] <outfile>\n"
            + " [OPTIONS]  \n"
            + "  -version          print FOP version and exit\n"
            + "  -d                debug mode   \n"
            + "  -x                dump configuration settings  \n"
            + "  -q                quiet mode  \n"
            + "  -c cfg.xml        use additional configuration file cfg.xml\n"
            + "  -l lang           the language to use for user information \n"
            + "  -nocs             disable complex script features\n"
            + "  -r                relaxed/less strict validation (where available)\n"
            + "  -dpi xxx          target resolution in dots per inch (dpi) where xxx is a number\n"
            + "  -s                for area tree XML, down to block areas only\n"
            + "  -v                run in verbose mode (currently simply print FOP version"
            + " and continue)\n\n"
            + "  -o [password]     PDF file will be encrypted with option owner password\n"
            + "  -u [password]     PDF file will be encrypted with option user password\n"
            + "  -noprint          PDF file will be encrypted without printing permission\n"
            + "  -nocopy           PDF file will be encrypted without copy content permission\n"
            + "  -noedit           PDF file will be encrypted without edit content permission\n"
            + "  -noannotations    PDF file will be encrypted without edit annotation permission\n"
            + "  -nofillinforms    PDF file will be encrypted without"
            + " fill in interactive form fields permission\n"
            + "  -noaccesscontent  PDF file will be encrypted without"
            + " extract text and graphics permission\n"
            + "  -noassembledoc    PDF file will be encrypted without"
            + " assemble the document permission\n"
            + "  -noprinthq        PDF file will be encrypted without"
            + " print high quality permission\n"
            + "  -a                enables accessibility features (Tagged PDF etc., default off)\n"
            + "  -pdfprofile prof  PDF file will be generated with the specified profile\n"
            + "                    (Examples for prof: PDF/A-1b or PDF/X-3:2003)\n\n"
            + "  -conserve         enable memory-conservation policy (trades memory-consumption"
            + " for disk I/O)\n"
            + "                    (Note: currently only influences whether the area tree is"
            + " serialized.)\n\n"

            + "  -cache            specifies a file/directory path location"
            + " for the font cache file\n"
            + "  -flush            flushes the current font cache file\n\n"

            + " [INPUT]  \n"
            + "  infile            xsl:fo input file (the same as the next) \n"
            + "                    (use '-' for infile to pipe input from stdin)\n"
            + "  -fo  infile       xsl:fo input file  \n"
            + "  -xml infile       xml input file, must be used together with -xsl \n"
            + "  -atin infile      area tree input file \n"
            + "  -ifin infile      intermediate format input file \n"
            + "  -imagein infile   image input file (piping through stdin not supported)\n"
            + "  -xsl stylesheet   xslt stylesheet \n \n"
            + "  -param name value <value> to use for parameter <name> in xslt stylesheet\n"
            + "                    (repeat '-param name value' for each parameter)\n \n"
            + "  -catalog          use catalog resolver for input XML and XSLT files\n"
            + " [OUTPUT] \n"
            + "  outfile           input will be rendered as PDF into outfile\n"
            + "                    (use '-' for outfile to pipe output to stdout)\n"
            + "  -pdf outfile      input will be rendered as PDF (outfile req'd)\n"
            + "  -pdfa1b outfile   input will be rendered as PDF/A-1b compliant PDF\n"
            + "                    (outfile req'd, same as \"-pdf outfile -pdfprofile PDF/A-1b\")\n"
            + "  -awt              input will be displayed on screen \n"
            + "  -rtf outfile      input will be rendered as RTF (outfile req'd)\n"
            + "  -pcl outfile      input will be rendered as PCL (outfile req'd) \n"
            + "  -ps outfile       input will be rendered as PostScript (outfile req'd) \n"
            + "  -afp outfile      input will be rendered as AFP (outfile req'd)\n"
            + "  -tiff outfile     input will be rendered as TIFF (outfile req'd)\n"
            + "  -png outfile      input will be rendered as PNG (outfile req'd)\n"
            + "  -txt outfile      input will be rendered as plain text (outfile req'd) \n"
            + "  -at [mime] out    representation of area tree as XML (outfile req'd) \n"
            + "                    specify optional mime output to allow the AT to be converted\n"
            + "                    to final format later\n"
            + "  -if [mime] out    representation of document in intermediate format XML"
            + " (outfile req'd)\n"
            + "                    specify optional mime output to allow the IF to be converted\n"
            + "                    to final format later\n"
            + "  -print            input file will be rendered and sent to the printer \n"
            + "                    see options with \"-print help\" \n"
            + "  -out mime outfile input will be rendered using the given MIME type\n"
            + "                    (outfile req'd) Example: \"-out application/pdf D:\\out.pdf\"\n"
            + "                    (Tip: \"-out list\" prints the list of supported MIME types"
            + " and exits)\n"
            //+ "  -mif outfile      input will be rendered as MIF (FrameMaker) (outfile req'd)\n"
            //+ "                    Experimental feature - requires additional fop-sandbox.jar.\n"
            + "  -svg outfile      input will be rendered as an SVG slides file (outfile req'd) \n"
            + "                    Experimental feature - requires additional fop-sandbox.jar.\n"
            + "\n"
            + "  -foout outfile    input will only be XSL transformed. The intermediate \n"
            + "                    XSL-FO file is saved and no rendering is performed. \n"
            + "                    (Only available if you use -xml and -xsl parameters)\n\n"
            + "\n"
            + " [Examples]\n" + "  fop foo.fo foo.pdf \n"
            + "  fop -fo foo.fo -pdf foo.pdf (does the same as the previous line)\n"
            + "  fop -xml foo.xml -xsl foo.xsl -pdf foo.pdf\n"
            + "  fop -xml foo.xml -xsl foo.xsl -foout foo.fo\n"
            + "  fop -xml - -xsl foo.xsl -pdf -\n"
            + "  fop foo.fo -mif foo.mif\n"
            + "  fop foo.fo -rtf foo.rtf\n"
            + "  fop foo.fo -print\n"
            + "  fop foo.fo -awt\n");
    }

    /**
     * shows the options for print output
     */
    private void printUsagePrintOutput() {
        System.err.println("USAGE: -print [from[-to][,even|odd]] [-copies numCopies]\n\n"
           + "Example:\n"
           + "all pages:                        fop infile.fo -print\n"
           + "all pages with two copies:        fop infile.fo -print -copies 2\n"
           + "all pages starting with page 7:   fop infile.fo -print 7\n"
           + "pages 2 to 3:                     fop infile.fo -print 2-3\n"
           + "only even page between 10 and 20: fop infile.fo -print 10-20,even\n");
    }

    /**
     * Outputs all commandline settings
     */
    private void dumpConfiguration() {
        log.info("Input mode: ");
        switch (inputmode) {
        case NOT_SET:
            log.info("not set");
            break;
        case FO_INPUT:
            log.info("FO ");
            if (isInputFromStdIn()) {
                log.info("fo input file: from stdin");
            } else {
                log.info("fo input file: " + fofile.toString());
            }
            break;
        case XSLT_INPUT:
            log.info("xslt transformation");
            if (isInputFromStdIn()) {
                log.info("xml input file: from stdin");
            } else {
                log.info("xml input file: " + xmlfile.toString());
            }
            log.info("xslt stylesheet: " + xsltfile.toString());
            break;
        case AREATREE_INPUT:
            log.info("AT ");
            if (isInputFromStdIn()) {
                log.info("area tree input file: from stdin");
            } else {
                log.info("area tree input file: " + areatreefile.toString());
            }
            break;
        case IF_INPUT:
            log.info("IF ");
            if (isInputFromStdIn()) {
                log.info("intermediate input file: from stdin");
            } else {
                log.info("intermediate input file: " + iffile.toString());
            }
            break;
        case IMAGE_INPUT:
            log.info("Image ");
            if (isInputFromStdIn()) {
                log.info("image input file: from stdin");
            } else {
                log.info("image input file: " + imagefile.toString());
            }
            break;
        default:
            log.info("unknown input type");
        }
        log.info("Output mode: ");
        if (outputmode == null) {
            log.info("not set");
        } else if (MimeConstants.MIME_FOP_AWT_PREVIEW.equals(outputmode)) {
            log.info("awt on screen");
            if (outfile != null) {
                log.error("awt mode, but outfile is set:");
                log.error("out file: " + outfile.toString());
            }
        } else if (MimeConstants.MIME_FOP_PRINT.equals(outputmode)) {
            log.info("print directly");
            if (outfile != null) {
                log.error("print mode, but outfile is set:");
                log.error("out file: " + outfile.toString());
            }
        } else if (MimeConstants.MIME_FOP_AREA_TREE.equals(outputmode)) {
            log.info("area tree");
            if (mimicRenderer != null) {
              log.info("mimic renderer: " + mimicRenderer);
            }
            if (isOutputToStdOut()) {
                log.info("output file: to stdout");
            } else {
                log.info("output file: " + outfile.toString());
            }
        } else if (MimeConstants.MIME_FOP_IF.equals(outputmode)) {
            log.info("intermediate format");
            log.info("output file: " + outfile.toString());
        } else {
            log.info(outputmode);
            if (isOutputToStdOut()) {
                log.info("output file: to stdout");
            } else {
                log.info("output file: " + outfile.toString());
            }
        }

        log.info("OPTIONS");

        if (userConfigFile != null) {
            log.info("user configuration file: "
                                 + userConfigFile.toString());
        } else {
            log.info("no user configuration file is used [default]");
        }
    }

    private void flushCache() throws FOPException {
        factory.getFontManager().deleteCache();
    }
}

