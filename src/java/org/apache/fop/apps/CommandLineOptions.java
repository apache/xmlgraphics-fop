/*
 * $Id: CommandLineOptions.java,v 1.22 2003/02/27 10:13:06 jeremias Exp $
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
package org.apache.fop.apps;

// java
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

// Avalon
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;

/**
 * Options parses the commandline arguments
 */
public class CommandLineOptions {

    /** input / output not set */
    public static final int NOT_SET = 0;
    /** input: fo file */
    public static final int FO_INPUT = 1;
    /** input: xml+xsl file */
    public static final int XSLT_INPUT = 2;
    /** output: pdf file */
    public static final int PDF_OUTPUT = 1;
    /** output: screen using swing */
    public static final int AWT_OUTPUT = 2;
    /** output: mif file */
    public static final int MIF_OUTPUT = 3;
    /** output: sent swing rendered file to printer */
    public static final int PRINT_OUTPUT = 4;
    /** output: pcl file */
    public static final int PCL_OUTPUT = 5;
    /** output: postscript file */
    public static final int PS_OUTPUT = 6;
    /** output: text file */
    public static final int TXT_OUTPUT = 7;
    /** output: svg file */
    public static final int SVG_OUTPUT = 8;
    /** output: XML area tree */
    public static final int AREA_OUTPUT = 9;
    /** output: RTF file */
    public static final int RTF_OUTPUT = 10;

    /* show configuration information */
    private Boolean dumpConfiguration = Boolean.FALSE;
    /* suppress any progress information */
    private Boolean quiet = Boolean.FALSE;
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

    private java.util.HashMap rendererOptions;

    private Logger log;

    /**
     * Construct a command line option object from command line arguments
     * @param args command line parameters
     * @throws FOPException for general errors
     * @throws FileNotFoundException if an input file wasn't found.
     */
    public CommandLineOptions(String[] args)
            throws FOPException, FileNotFoundException {

        log = new ConsoleLogger(ConsoleLogger.LEVEL_INFO);

        boolean optionsParsed = true;
        rendererOptions = new java.util.HashMap();
        try {
            optionsParsed = parseOptions(args);
            if (optionsParsed) {
                checkSettings();
            }
        } catch (FOPException e) {
            printUsage();
            throw e;
        } catch (java.io.FileNotFoundException e) {
            printUsage();
            throw e;
        }
    }

    /**
     * Get the logger.
     * @return the logger
     */
    public Logger getLogger() {
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
            if (args[i].equals("-d") || args[i].equals("--full-error-dump")) {
                log = new ConsoleLogger(ConsoleLogger.LEVEL_DEBUG);
            } else if (args[i].equals("-x")
                       || args[i].equals("--dump-config")) {
                dumpConfiguration = Boolean.TRUE;
            } else if (args[i].equals("-q") || args[i].equals("--quiet")) {
                quiet = Boolean.TRUE;
                log = new ConsoleLogger(ConsoleLogger.LEVEL_ERROR);
            } else if (args[i].equals("-c")) {
                i = i + parseConfigurationOption(args, i);
            } else if (args[i].equals("-l")) {
                i = i + parseLanguageOption(args, i);
            } else if (args[i].equals("-s")) {
                suppressLowLevelAreas = Boolean.TRUE;
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
        setOutputMode(AWT_OUTPUT);
        return 0;
    }

    private int parsePDFOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(PDF_OUTPUT);
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("you must specify the pdf output file");
        } else {
            outfile = new File(args[i + 1]);
            return 1;
        }
    }

    private int parseMIFOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(MIF_OUTPUT);
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("you must specify the mif output file");
        } else {
            outfile = new File(args[i + 1]);
            return 1;
        }
    }

    private int parseRTFOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(RTF_OUTPUT);
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("you must specify the rtf output file");
        } else {
            outfile = new File(args[i + 1]);
            return 1;
        }
    }

    private int parsePrintOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(PRINT_OUTPUT);
        return 0;
    }

    private int parsePCLOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(PCL_OUTPUT);
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("you must specify the pdf output file");
        } else {
            outfile = new File(args[i + 1]);
            return 1;
        }
    }

    private int parsePostscriptOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(PS_OUTPUT);
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("you must specify the PostScript output file");
        } else {
            outfile = new File(args[i + 1]);
            return 1;
        }
    }

    private int parseTextOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(TXT_OUTPUT);
        if ((i + 1 == args.length)
                || (args[i + 1].charAt(0) == '-')) {
            throw new FOPException("you must specify the text output file");
        } else {
            outfile = new File(args[i + 1]);
            return 1;
        }
    }

    private int parseSVGOutputOption(String[] args, int i) throws FOPException {
        setOutputMode(SVG_OUTPUT);
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
            outputmode = PDF_OUTPUT;
            outfile = new File(args[i]);
        } else {
            throw new FOPException("Don't know what to do with "
                           + args[i]);
        }
        return 0;
    }

    private int parseAreaTreeOption(String[] args, int i) throws FOPException {
        setOutputMode(AREA_OUTPUT);
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
     * @return the type chosen renderer
     * @throws FOPException for invalid output modes
     */
    public int getRenderer() throws FOPException {
        switch (outputmode) {
        case NOT_SET:
            throw new FOPException("Renderer has not been set!");
        case PDF_OUTPUT:
            return Driver.RENDER_PDF;
        case AWT_OUTPUT:
            return Driver.RENDER_AWT;
        case MIF_OUTPUT:
            return Driver.RENDER_MIF;
        case PRINT_OUTPUT:
            return Driver.RENDER_PRINT;
        case PCL_OUTPUT:
            return Driver.RENDER_PCL;
        case PS_OUTPUT:
            return Driver.RENDER_PS;
        case TXT_OUTPUT:
            return Driver.RENDER_TXT;
        case SVG_OUTPUT:
            return Driver.RENDER_SVG;
        case AREA_OUTPUT:
            rendererOptions.put("fineDetail", isCoarseAreaXml());
            return Driver.RENDER_XML;
        case RTF_OUTPUT:
            return Driver.RENDER_RTF;
        default:
            throw new FOPException("Invalid Renderer setting!");
        }
    }

    /**
     * Get the input handler.
     * @return the input handler
     * @throws FOPException if creating the InputHandler fails
     */
    public InputHandler getInputHandler() throws FOPException {
        switch (inputmode) {
        case FO_INPUT:
            return new FOFileHandler(fofile);
        case XSLT_INPUT:
            return new XSLTInputHandler(xmlfile, xsltfile);
        default:
            throw new FOPException("Invalid inputmode setting!");
        }
    }

    /**
     * Get the renderer specific options.
     * @return hash map with option/value pairs.
     */
    public java.util.HashMap getRendererOptions() {
        return rendererOptions;
    }

    /**
     * Returns the input mode (type of input data, ex. NOT_SET or FO_INPUT)
     * @return the input mode
     */
    public int getInputMode() {
        return inputmode;
    }

    /**
     * Returns the output mode (output format, ex. NOT_SET or PDF_OUTPUT)
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
     * Indicates if FOP should be silent.
     * @return true if should be silent
     */
    public Boolean isQuiet() {
        return quiet;
    }

    /**
     * Indicates if FOP should dump its configuration during runtime.
     * @return true if config dump is enabled
     */
    public Boolean dumpConfiguration() {
        return dumpConfiguration;
    }

    /**
     * Indicates whether the XML renderer should generate course area XML
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
            + "  -s          for area tree XML, down to block areas only\n\n"
            + " [INPUT]  \n"
            + "  infile            xsl:fo input file (the same as the next) \n"
            + "  -fo  infile       xsl:fo input file  \n"
            + "  -xml infile       xml input file, must be used together with -xsl \n"
            + "  -xsl stylesheet   xslt stylesheet \n \n"
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
    public void printUsagePrintOutput() {
        System.err.println("USAGE: -print [-Dstart=i] [-Dend=i] [-Dcopies=i] [-Deven=true|false] "
                           + " org.apache.fop.apps.Fop (..) -print \n"
                           + "Example:\n"
                           + "java -Dstart=1 -Dend=2 org.apache.Fop.apps.Fop infile.fo -print ");
    }


    /**
     * debug mode. outputs all commandline settings
     */
    private void debug() {
        log.debug("Input mode: ");
        switch (inputmode) {
        case NOT_SET:
            log.debug("not set");
            break;
        case FO_INPUT:
            log.debug("FO ");
            log.debug("fo input file: " + fofile.toString());
            break;
        case XSLT_INPUT:
            log.debug("xslt transformation");
            log.debug("xml input file: " + xmlfile.toString());
            log.debug("xslt stylesheet: " + xsltfile.toString());
            break;
        default:
            log.debug("unknown input type");
        }
        log.debug("Output mode: ");
        switch (outputmode) {
        case NOT_SET:
            log.debug("not set");
            break;
        case PDF_OUTPUT:
            log.debug("pdf");
            log.debug("output file: " + outfile.toString());
            break;
        case AWT_OUTPUT:
            log.debug("awt on screen");
            if (outfile != null) {
                log.error("awt mode, but outfile is set:");
                log.debug("out file: " + outfile.toString());
            }
            break;
        case MIF_OUTPUT:
            log.debug("mif");
            log.debug("output file: " + outfile.toString());
            break;
        case RTF_OUTPUT:
            log.debug("rtf");
            log.debug("output file: " + outfile.toString());
            break;
        case PRINT_OUTPUT:
            log.debug("print directly");
            if (outfile != null) {
                log.error("print mode, but outfile is set:");
                log.error("out file: " + outfile.toString());
            }
            break;
        case PCL_OUTPUT:
            log.debug("pcl");
            log.debug("output file: " + outfile.toString());
            break;
        case PS_OUTPUT:
            log.debug("PostScript");
            log.debug("output file: " + outfile.toString());
            break;
        case TXT_OUTPUT:
            log.debug("txt");
            log.debug("output file: " + outfile.toString());
            break;
        case SVG_OUTPUT:
            log.debug("svg");
            log.debug("output file: " + outfile.toString());
            break;
        default:
            log.debug("unknown input type");
        }


        log.debug("OPTIONS");
        if (userConfigFile != null) {
            log.debug("user configuration file: "
                                 + userConfigFile.toString());
        } else {
            log.debug("no user configuration file is used [default]");
        }
        if (dumpConfiguration != null) {
            log.debug("dump configuration");
        } else {
            log.debug("don't dump configuration [default]");
        }
        if (quiet != null) {
            log.debug("quiet mode on");
        } else {
            log.debug("quiet mode off [default]");
        }

    }

}

