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

package org.apache.fop.apps;

// java
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Vector;

import org.apache.fop.fo.Constants;
import org.apache.fop.util.CommandLineLogger;

// commons logging
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// SAX
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import javax.xml.parsers.SAXParserFactory;

// avalon configuration
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * Options parses the commandline arguments
 */
public class CommandLineOptions implements Constants {

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
    /* output file */
    private File outfile = null;
    /* input mode */
    private int inputmode = NOT_SET;
    /* output mode */
    private int outputmode = NOT_SET;

    private FOUserAgent foUserAgent;
    
    private Log log;

    private Vector xsltParams = null;
    
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
     * Parse the command line arguments.
     * @param args the command line arguments.
     * @throws FOPException for general errors
     * @throws FileNotFoundException if an input file wasn't found
     * @throws IOException if the the configuration file could not be loaded
     */
    public void parse(String[] args) 
            throws FOPException, IOException {
        boolean optionsParsed = true;
        
        foUserAgent = new FOUserAgent();
        
        try {
            optionsParsed = parseOptions(args);
            if (optionsParsed) {
                if (showConfiguration == Boolean.TRUE) {
                    dumpConfiguration();
                }
                checkSettings();
                createUserConfig();
            }
        } catch (FOPException e) {
            printUsage();
            throw e;
        } catch (java.io.FileNotFoundException e) {
            printUsage();
            throw e;
        }
        
        foUserAgent.setInputHandler(createInputHandler());
    }

    /**
     * Get the logger.
     * @return the logger
     */
    public Log getLogger() {
        return log;
    }

    /**
     * parses the commandline arguments
     * @return true if parse was successful and processing can continue, false
     * if processing should stop
     * @exception FOPException if there was an error in the format of the options
     */
    private boolean parseOptions(String[] args) throws FOPException {
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
                setLogLevel("debug");
            } else if (args[i].equals("-r")) {
                foUserAgent.setStrictValidation(false);
            } else if (args[i].equals("-q") || args[i].equals("--quiet")) {
                setLogLevel("error");
            } else if (args[i].equals("-fo")) {
                i = i + parseFOInputOption(args, i);
            } else if (args[i].equals("-xsl")) {
                i = i + parseXSLInputOption(args, i);
            } else if (args[i].equals("-xml")) {
                i = i + parseXMLInputOption(args, i);
            } else if (args[i].equals("-awt")) {
                i = i + parseAWTOutputOption(args, i);
            } else if (args[i].equals("-pdf")) {
                i = i + parsePDFOutputOption(args, i);
            } else if (args[i].equals("-mif")) {
                i = i + parseMIFOutputOption(args, i);
            } else if (args[i].equals("-rtf")) {
                i = i + parseRTFOutputOption(args, i);
            } else if (args[i].equals("-print")) {
                i = i + parsePrintOutputOption(args, i);
                // show print help
                if (i + 1 < args.length) {
                    if (args[i + 1].equals("help")) {
                        printUsagePrintOutput();
                        return false;
                    }
                }
            } else if (args[i].equals("-pcl")) {
                i = i + parsePCLOutputOption(args, i);
            } else if (args[i].equals("-ps")) {
                i = i + parsePostscriptOutputOption(args, i);
            } else if (args[i].equals("-txt")) {
                i = i + parseTextOutputOption(args, i);
            } else if (args[i].equals("-svg")) {
                i = i + parseSVGOutputOption(args, i);
            } else if (args[i].charAt(0) != '-') {
                i = i + parseUnknownOption(args, i);
            } else if (args[i].equals("-at")) {
                i = i + parseAreaTreeOption(args, i);
            } else if (args[i].equals("-v")) {
                System.out.println("FOP Version " + Fop.getVersion());
            } else if (args[i].equals("-param")) {
                  if (i + 2 < args.length) {
                      if (xsltParams == null) {
                          xsltParams = new Vector();
                      }
                      String name = args[++i];
                      xsltParams.addElement(name);
                      String expression = args[++i];
                      xsltParams.addElement(expression);
                  } else {
                    throw new FOPException("invalid param usage: use -param <name> <value>");
                  }
            } else {
                printUsage();
                return false;
            }
        }
        return true;
    }    // end parseOptions

    private int parseConfigurationOption(String[] args, int i) throws FOPException {
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("if you use '-c', you must specify "
              + "the name of the configuration file");
        } else {
            userConfigFile = new File(args[i + 1]);
            return 1;
        }
    }

    private int parseLanguageOption(String[] args, int i) throws FOPException {
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("if you use '-l', you must specify a language");
        } else {
            Locale.setDefault(new Locale(args[i + 1], ""));
            return 1;
        }
    }

    private int parseFOInputOption(String[] args, int i) throws FOPException {
        inputmode = FO_INPUT;
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("you must specify the fo file for the '-fo' option");
        } else {
            fofile = new File(args[i + 1]);
            return 1;
        }
    }

    private int parseXSLInputOption(String[] args, int i) throws FOPException {
        inputmode = XSLT_INPUT;
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("you must specify the stylesheet "
                            + "file for the '-xsl' option");
        } else {
            xsltfile = new File(args[i + 1]);
            return 1;
        }
    }

    private int parseXMLInputOption(String[] args, int i) throws FOPException {
        inputmode = XSLT_INPUT;
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("you must specify the input file "
                            + "for the '-xml' option");
        } else {
            xmlfile = new File(args[i + 1]);
            return 1;
        }
    }

    private int parseAWTOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(RENDER_AWT);
        return 0;
    }

    private int parsePDFOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(RENDER_PDF);
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("you must specify the pdf output file");
        } else {
            outfile = new File(args[i + 1]);
            return 1;
        }
    }

    private int parseMIFOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(RENDER_MIF);
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("you must specify the mif output file");
        } else {
            outfile = new File(args[i + 1]);
            return 1;
        }
    }

    private int parseRTFOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(RENDER_RTF);
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("you must specify the rtf output file");
        } else {
            outfile = new File(args[i + 1]);
            return 1;
        }
    }

    private int parsePrintOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(RENDER_PRINT);
        return 0;
    }

    private int parsePCLOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(RENDER_PCL);
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("you must specify the pdf output file");
        } else {
            outfile = new File(args[i + 1]);
            return 1;
        }
    }

    private int parsePostscriptOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(RENDER_PS);
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("you must specify the PostScript output file");
        } else {
            outfile = new File(args[i + 1]);
            return 1;
        }
    }

    private int parseTextOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(RENDER_TXT);
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("you must specify the text output file");
        } else {
            outfile = new File(args[i + 1]);
            return 1;
        }
    }

    private int parseSVGOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(RENDER_SVG);
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("you must specify the svg output file");
        } else {
            outfile = new File(args[i + 1]);
            return 1;
        }
    }

    private int parseUnknownOption(String[] args, int i) throws FOPException {
        if (inputmode == NOT_SET) {
            inputmode = FO_INPUT;
            fofile = new File(args[i]);
        } else if (outputmode == NOT_SET) {
            outputmode = RENDER_PDF;
            outfile = new File(args[i]);
        } else {
            throw new FOPException("Don't know what to do with "
                           + args[i]);
        }
        return 0;
    }

    private int parseAreaTreeOption(String[] args, int i) throws FOPException {
        setOutputMode(RENDER_XML);
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("you must specify the area-tree output file");
        } else {
            outfile = new File(args[i + 1]);
            return 1;
        }
    }

    private void setOutputMode(int mode) throws FOPException {
        if (outputmode == NOT_SET) {
            outputmode = mode;
        } else {
            throw new FOPException("you can only set one output method");
        }
    }

    private void setLogLevel(String level) {
        // Set the evel for future loggers.
        LogFactory.getFactory().setAttribute("level", level);
        if (log instanceof CommandLineLogger) {
            // Set the level for the logger creates already.
            ((CommandLineLogger) log).setLogLevel(level);
        }
    }
        
    /**
     * checks whether all necessary information has been given in a consistent way
     */
    private void checkSettings() throws FOPException, FileNotFoundException {
        if (inputmode == NOT_SET) {
            throw new FOPException("No input file specified");
        }

        if (outputmode == NOT_SET) {
            throw new FOPException("No output file specified");
        }

        if ((outputmode == RENDER_AWT || outputmode == RENDER_PRINT) && outfile != null) {
            throw new FOPException("Output file may not be specified " 
                    + "for AWT or PRINT output");
        }

        if (inputmode == XSLT_INPUT) {
            // check whether xml *and* xslt file have been set
            if (xmlfile == null) {
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
            if (!xmlfile.exists()) {
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
            if (xmlfile != null || xsltfile != null) {
                log.warn("fo input mode, but xmlfile or xslt file are set:");
                log.error("xml file: " + xmlfile.toString());
                log.error("xslt file: " + xsltfile.toString());
            }
            if (!fofile.exists()) {
                throw new FileNotFoundException("Error: fo file "
                                                + fofile.getAbsolutePath()
                                                + " not found ");
            }

        }
    }    // end checkSettings

    /**
     * Create the user configuration.
     * @throws FOPException if creating the user configuration fails
     * @throws IOException
     */
    private void createUserConfig() throws FOPException, IOException {
        if (userConfigFile == null) {
            return;
        }
        XMLReader parser = createParser();
        DefaultConfigurationBuilder configBuilder
            = new DefaultConfigurationBuilder(parser);
        Configuration userConfig = null;
        try {
            userConfig = configBuilder.buildFromFile(userConfigFile);
        } catch (SAXException e) {
            throw new FOPException(e);
        } catch (ConfigurationException e) {
            throw new FOPException(e);
        }
        foUserAgent.setUserConfig(userConfig);
     }

    /**
     * @return the type chosen renderer
     * @throws FOPException for invalid output modes
     */
    protected int getRenderer() throws FOPException {
        switch (outputmode) {
        case RENDER_PDF:
        case RENDER_AWT:
        case RENDER_MIF:
        case RENDER_PRINT:
        case RENDER_PCL:
        case RENDER_PS:
        case RENDER_TXT:
        case RENDER_SVG:
        case RENDER_RTF:
            return outputmode;
        case RENDER_XML:
            foUserAgent.getRendererOptions().put("fineDetail", isCoarseAreaXml());
            return RENDER_XML;
        case NOT_SET:
            throw new FOPException("Renderer has not been set!");
        default:
            throw new FOPException("Invalid Renderer setting!");
        }
    }

    /**
     * Create an InputHandler object based on command-line parameters
     * @return a new InputHandler instance
     * @throws IllegalArgumentException if invalid/missing parameters
     */
    private InputHandler createInputHandler() throws IllegalArgumentException {
        switch (inputmode) {
            case FO_INPUT:
                return new InputHandler(fofile);
            case XSLT_INPUT:
                return new InputHandler(xmlfile, xsltfile, xsltParams);
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
     * Returns the output mode (output format, ex. NOT_SET or RENDER_PDF)
     * @return the output mode
     */
    public int getOutputMode() {
        return outputmode;
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

    /**
     * shows the commandline syntax including a summary of all available options and some examples
     */
    public static void printUsage() {
        System.err.println(
              "\nUSAGE\nFop [options] [-fo|-xml] infile [-xsl file] "
                    + "[-awt|-pdf|-mif|-rtf|-pcl|-ps|-txt|-at|-print] <outfile>\n"
            + " [OPTIONS]  \n"
            + "  -d          debug mode   \n"
            + "  -x          dump configuration settings  \n"
            + "  -q          quiet mode  \n"
            + "  -c cfg.xml  use additional configuration file cfg.xml\n"
            + "  -l lang     the language to use for user information \n"
            + "  -r          relaxed/less strict validation (where available)\n"
            + "  -s          for area tree XML, down to block areas only\n"
            + "  -v          to show FOP version being used\n\n"
            + " [INPUT]  \n"
            + "  infile            xsl:fo input file (the same as the next) \n"
            + "  -fo  infile       xsl:fo input file  \n"
            + "  -xml infile       xml input file, must be used together with -xsl \n"
            + "  -xsl stylesheet   xslt stylesheet \n \n"
            + "  -param name value <value> to use for parameter <name> in xslt stylesheet\n"
            + "                    (repeat '-param name value' for each parameter)\n \n" 
            + " [OUTPUT] \n"
            + "  outfile           input will be rendered as pdf file into outfile \n"
            + "  -pdf outfile      input will be rendered as pdf file (outfile req'd) \n"
            + "  -awt              input will be displayed on screen \n"
            + "  -mif outfile      input will be rendered as mif file (outfile req'd)\n"
            + "  -rtf outfile      input will be rendered as rtf file (outfile req'd)\n"
            + "  -pcl outfile      input will be rendered as pcl file (outfile req'd) \n"
            + "  -ps outfile       input will be rendered as PostScript file (outfile req'd) \n"
            + "  -txt outfile      input will be rendered as text file (outfile req'd) \n"
            + "  -svg outfile      input will be rendered as an svg slides file (outfile req'd) \n"
            + "  -at outfile       representation of area tree as XML (outfile req'd) \n"
            + "  -print            input file will be rendered and sent to the printer \n"
            + "                    see options with \"-print help\" \n\n"
            + " [Examples]\n" + "  Fop foo.fo foo.pdf \n"
            + "  Fop -fo foo.fo -pdf foo.pdf (does the same as the previous line)\n"
            + "  Fop -xml foo.xml -xsl foo.xsl -pdf foo.pdf\n"
            + "  Fop foo.fo -mif foo.mif\n"
            + "  Fop foo.fo -rtf foo.rtf\n"
            + "  Fop foo.fo -print or Fop -print foo.fo \n"
            + "  Fop foo.fo -awt \n");
    }

    /**
     * shows the options for print output
     */
    private void printUsagePrintOutput() {
        System.err.println("USAGE: -print [-Dstart=i] [-Dend=i] [-Dcopies=i] [-Deven=true|false] "
                           + " org.apache.fop.apps.Fop (..) -print \n"
                           + "Example:\n"
                           + "java -Dstart=1 -Dend=2 org.apache.Fop.apps.Fop infile.fo -print ");
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
            log.info("fo input file: " + fofile.toString());
            break;
        case XSLT_INPUT:
            log.info("xslt transformation");
            log.info("xml input file: " + xmlfile.toString());
            log.info("xslt stylesheet: " + xsltfile.toString());
            break;
        default:
            log.info("unknown input type");
        }
        log.info("Output mode: ");
        switch (outputmode) {
        case NOT_SET:
            log.info("not set");
            break;
        case RENDER_PDF:
            log.info("pdf");
            log.info("output file: " + outfile.toString());
            break;
        case RENDER_AWT:
            log.info("awt on screen");
            if (outfile != null) {
                log.error("awt mode, but outfile is set:");
                log.info("out file: " + outfile.toString());
            }
            break;
        case RENDER_MIF:
            log.info("mif");
            log.info("output file: " + outfile.toString());
            break;
        case RENDER_RTF:
            log.info("rtf");
            log.info("output file: " + outfile.toString());
            break;
        case RENDER_PRINT:
            log.info("print directly");
            if (outfile != null) {
                log.error("print mode, but outfile is set:");
                log.error("out file: " + outfile.toString());
            }
            break;
        case RENDER_PCL:
            log.info("pcl");
            log.info("output file: " + outfile.toString());
            break;
        case RENDER_PS:
            log.info("PostScript");
            log.info("output file: " + outfile.toString());
            break;
        case RENDER_TXT:
            log.info("txt");
            log.info("output file: " + outfile.toString());
            break;
        case RENDER_SVG:
            log.info("svg");
            log.info("output file: " + outfile.toString());
            break;
        default:
            log.info("unknown input type");
        }

        log.info("OPTIONS");
        
        if (userConfigFile != null) {
            log.info("user configuration file: "
                                 + userConfigFile.toString());
        } else {
            log.info("no user configuration file is used [default]");
        }
    }

    /**
     * Creates <code>XMLReader</code> object using default
     * <code>SAXParserFactory</code>
     * @return the created <code>XMLReader</code>
     * @throws FOPException if the parser couldn't be created or configured for proper operation.
     */
    private XMLReader createParser() throws FOPException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory.newSAXParser().getXMLReader();
        } catch (Exception e) {
            throw new FOPException("Couldn't create XMLReader", e);
        }
    }
}

