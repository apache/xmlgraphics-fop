/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;

// java
import java.util.Vector;
import java.io.File;
import java.io.FileNotFoundException;

// FOP
import org.apache.fop.configuration.Configuration;
import org.apache.fop.apps.FOPException;

// Avalon
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;

import java.io.*;

/**
 * Options parses the commandline arguments
 */
public class CommandLineOptions {

    /* input / output not set */
    private static final int NOT_SET = 0;
    /* input: fo file */
    private static final int FO_INPUT = 1;
    /* input: xml+xsl file */
    private static final int XSLT_INPUT = 2;
    /* output: pdf file */
    private static final int PDF_OUTPUT = 1;
    /* output: screen using swing */
    private static final int AWT_OUTPUT = 2;
    /* output: mif file */
    private static final int MIF_OUTPUT = 3;
    /* output: sent swing rendered file to printer */
    private static final int PRINT_OUTPUT = 4;
    /* output: pcl file */
    private static final int PCL_OUTPUT = 5;
    /* output: postscript file */
    private static final int PS_OUTPUT = 6;
    /* output: text file */
    private static final int TXT_OUTPUT = 7;
    /* output: svg file */
    private static final int SVG_OUTPUT = 8;

    /* System buffers */
    private static final int BUFFER_FILE = 8;

    /* System buffers */
    private static final int AREA_OUTPUT = 9;

    /* use debug mode */
    Boolean errorDump = new Boolean(false);
    /* show configuration information */
    Boolean dumpConfiguration = new Boolean(false);
    /* suppress any progress information */
    Boolean quiet = new Boolean(false);
    /* for area tree XML output, only down to block area level */
    Boolean suppressLowLevelAreas = new Boolean(false);
    /* name of user configuration file */
    File userConfigFile = null;
    /* name of input fo file */
    File fofile = null;
    /* name of xsltfile (xslt transformation as input) */
    File xsltfile = null;
    /* name of xml file (xslt transformation as input) */
    File xmlfile = null;
    /* name of output file */
    File outfile = null;
    /* name of buffer file */
    File bufferFile = null;
    /* input mode */
    int inputmode = NOT_SET;
    /* output mode */
    int outputmode = NOT_SET;
    /* buffer mode */
    int buffermode = NOT_SET;
    /* language for user information */
    String language = null;

    private java.util.HashMap rendererOptions;

    private Logger log;

    public CommandLineOptions(String[] args)
            throws FOPException, FileNotFoundException {

        log = new ConsoleLogger(ConsoleLogger.LEVEL_INFO);

        boolean optionsParsed = true;
        rendererOptions = new java.util.HashMap();
        try {
            optionsParsed = parseOptions(args);
            if (optionsParsed) {
                checkSettings();
                if (errorDump != null && errorDump.booleanValue()) {
                    debug();
                }
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
     * parses the commandline arguments
     * @return true if parse was successful and procesing can continue, false if processing should stop
     * @exception FOPException if there was an error in the format of the options
     */
    private boolean parseOptions(String args[]) throws FOPException {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-d") || args[i].equals("--full-error-dump")) {
                errorDump = new Boolean(true);
                log = new ConsoleLogger(ConsoleLogger.LEVEL_DEBUG);
            } else if (args[i].equals("-x")
                       || args[i].equals("--dump-config")) {
                dumpConfiguration = new Boolean(true);
            } else if (args[i].equals("-q") || args[i].equals("--quiet")) {
                quiet = new Boolean(true);
                log = new ConsoleLogger(ConsoleLogger.LEVEL_ERROR);
            } else if (args[i].equals("-c")) {
                if ((i + 1 == args.length)
                        || (args[i + 1].charAt(0) == '-')) {
                    throw new FOPException("if you use '-c', you must specify the name of the configuration file");
                } else {
                    userConfigFile = new File(args[i + 1]);
                    i++;
                }
            } else if (args[i].equals("-l")) {
                if ((i + 1 == args.length)
                        || (args[i + 1].charAt(0) == '-')) {
                    throw new FOPException("if you use '-l', you must specify a language");
                } else {
                    language = args[i + 1];
                    i++;
                }
            } else if (args[i].equals("-s")) {
                suppressLowLevelAreas = new Boolean(true);
            } else if (args[i].equals("-fo")) {
                inputmode = FO_INPUT;
                if ((i + 1 == args.length)
                        || (args[i + 1].charAt(0) == '-')) {
                    throw new FOPException("you must specify the fo file for the '-fo' option");
                } else {
                    fofile = new File(args[i + 1]);
                    i++;
                }
            } else if (args[i].equals("-xsl")) {
                inputmode = XSLT_INPUT;
                if ((i + 1 == args.length)
                        || (args[i + 1].charAt(0) == '-')) {
                    throw new FOPException("you must specify the stylesheet file for the '-xsl' option");
                } else {
                    xsltfile = new File(args[i + 1]);
                    i++;
                }
            } else if (args[i].equals("-xml")) {
                inputmode = XSLT_INPUT;
                if ((i + 1 == args.length)
                        || (args[i + 1].charAt(0) == '-')) {
                    throw new FOPException("you must specify the input file for the '-xml' option");
                } else {
                    xmlfile = new File(args[i + 1]);
                    i++;
                }
            } else if (args[i].equals("-awt")) {
                setOutputMode(AWT_OUTPUT);
            } else if (args[i].equals("-pdf")) {
                setOutputMode(PDF_OUTPUT);
                if ((i + 1 == args.length)
                        || (args[i + 1].charAt(0) == '-')) {
                    throw new FOPException("you must specify the pdf output file");
                } else {
                    outfile = new File(args[i + 1]);
                    i++;
                }
            } else if (args[i].equals("-mif")) {
                setOutputMode(MIF_OUTPUT);
                if ((i + 1 == args.length)
                        || (args[i + 1].charAt(0) == '-')) {
                    throw new FOPException("you must specify the mif output file");
                } else {
                    outfile = new File(args[i + 1]);
                    i++;
                }
            } else if (args[i].equals("-print")) {
                setOutputMode(PRINT_OUTPUT);
                // show print help
                if (i + 1 < args.length) {
                    if (args[i + 1].equals("help")) {
                        printUsagePrintOutput();
                        return false;
                    }
                }
            } else if (args[i].equals("-pcl")) {
                setOutputMode(PCL_OUTPUT);
                if ((i + 1 == args.length)
                        || (args[i + 1].charAt(0) == '-')) {
                    throw new FOPException("you must specify the pdf output file");
                } else {
                    outfile = new File(args[i + 1]);
                    i++;
                }
            } else if (args[i].equals("-ps")) {
                setOutputMode(PS_OUTPUT);
                if ((i + 1 == args.length)
                        || (args[i + 1].charAt(0) == '-')) {
                    throw new FOPException("you must specify the PostScript output file");
                } else {
                    outfile = new File(args[i + 1]);
                    i++;
                }
            } else if (args[i].equals("-txt")) {
                setOutputMode(TXT_OUTPUT);
                if ((i + 1 == args.length)
                        || (args[i + 1].charAt(0) == '-')) {
                    throw new FOPException("you must specify the text output file");
                } else {
                    outfile = new File(args[i + 1]);
                    i++;
                }
            } else if (args[i].equals("-svg")) {
                setOutputMode(SVG_OUTPUT);
                if ((i + 1 == args.length)
                        || (args[i + 1].charAt(0) == '-')) {
                    throw new FOPException("you must specify the svg output file");                } else {
                    outfile = new File(args[i + 1]);
                    i++;
                }
            } else if (args[i].charAt(0) != '-') {
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
            } else if (args[i].equals("-buf")) {
                if (buffermode == NOT_SET) {
                    buffermode = BUFFER_FILE;
                } else {
                    log.error("you can only set one buffer method");
                    printUsage();
                }
                if ((i + 1 == args.length)
                        || (args[i + 1].charAt(0) == '-')) {
                    log.error("you must specify the buffer output file");
                    printUsage();
                } else {
                    bufferFile = new File(args[i + 1]);
                    i++;
                }
            } else if (args[i].equals("-at")) {
                setOutputMode(AREA_OUTPUT);
                if ((i + 1 == args.length)
                        || (args[i + 1].charAt(0) == '-')) {
                    throw new FOPException("you must specify the area-tree output file");
                } else {
                    outfile = new File(args[i + 1]);
                    i++;
                }
            } else {
                printUsage();
                return false;
            }
        }
        return true;
    }    // end parseOptions

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
                throw new FOPException("XML file must be specified for the tranform mode");
            }
            if (xsltfile == null) {
                throw new FOPException("XSLT file must be specified for the tranform mode");
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
                throw new FileNotFoundException("xml file "
                                                + xmlfile.getAbsolutePath()
                                                + " not found ");
            }
            if (!xsltfile.exists()) {
                throw new FileNotFoundException("xsl file "
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
                throw new FileNotFoundException("fo file "
                                                + fofile.getAbsolutePath()
                                                + " not found ");
            }

        }
    }    // end checkSettings

    /**
     * returns the chosen renderer, throws FOPException
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
        default:
            throw new FOPException("Invalid Renderer setting!");
        }
    }

    /**
     *
     */
    public InputHandler getInputHandler() {
        switch (inputmode) {
        case FO_INPUT:
            return new FOInputHandler(fofile);
        case XSLT_INPUT:
            return new XSLTInputHandler(xmlfile, xsltfile);
        default:
            return new FOInputHandler(fofile);
        }
    }

    public java.util.HashMap getRendererOptions() {
        return rendererOptions;
    }

    public Starter getStarter() throws FOPException {
        Starter starter = null;
        switch (outputmode) {
        case AWT_OUTPUT:
            try {
                starter = ((Starter)Class.forName("org.apache.fop.apps.AWTStarter").getConstructor(new Class[] {
                    CommandLineOptions.class
                }).newInstance(new Object[] {
                    this
                }));
            } catch (Exception e) {
                if (e instanceof FOPException) {
                    throw (FOPException)e;
                }
                throw new FOPException("AWTStarter could not be loaded.", e);
            }
        break;
        case PRINT_OUTPUT:
            try {
                starter = ((Starter)Class.forName("org.apache.fop.apps.PrintStarter").getConstructor(new Class[] {
                    CommandLineOptions.class
                }).newInstance(new Object[] {
                    this
                }));
            } catch (Exception e) {
                if (e instanceof FOPException) {
                    throw (FOPException)e;
                }
                throw new FOPException("PrintStarter could not be loaded.",
                                       e);
            }
        break;
        default:
            starter = new CommandLineStarter(this);
        }
        starter.setLogger(log);
        return starter;
    }

    public int getInputMode() {
        return inputmode;
    }

    public int getOutputMode() {
        return outputmode;
    }

    public File getFOFile() {
        return fofile;
    }

    public File getXMLFile() {
        return xmlfile;
    }

    public File getXSLFile() {
        return xsltfile;
    }

    public File getOutputFile() {
        return outfile;
    }

    public File getUserConfigFile() {
        return userConfigFile;
    }

    public String getLanguage() {
        return language;
    }

    public File getBufferFile() {
        return bufferFile;
    }

    public Boolean isQuiet() {
        return quiet;
    }

    public Boolean dumpConfiguration() {
        return dumpConfiguration;
    }

    public Boolean isDebugMode() {
        return errorDump;
    }

    public Boolean isCoarseAreaXml() {
        return suppressLowLevelAreas;
    }

    /**
     * return either the fofile or the xmlfile
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
        System.err.println("\nUSAGE\nFop [options] [-fo|-xml] infile [-xsl file] [-awt|-pdf|-mif|-pcl|-ps|-txt|-at|-print] <outfile>\n"
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
                               + "  -pcl outfile      input will be rendered as pcl file (outfile req'd) \n"
                               + "  -ps outfile       input will be rendered as PostScript file (outfile req'd) \n"
                               + "  -txt outfile      input will be rendered as text file (outfile req'd) \n"
                               + "  -svg outfile      input will be rendered as an svg slides file (outfile req'd) \n"
                               + "  -at outfile       representation of area tree as XML (outfile req'd) \n"
                               + "  -print            input file will be rendered and sent to the printer \n"
                               + "                    see options with \"-print help\" \n\n"
                               + " [Examples]\n" + "  Fop foo.fo foo.pdf \n"
                               + "  Fop -fo foo.fo -pdf foo.pdf (does the same as the previous line)\n"
                               + "  Fop -xsl foo.xsl -xml foo.xml -pdf foo.pdf\n"
                               + "  Fop foo.fo -mif foo.mif\n"
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
        if (errorDump != null) {
            log.debug("debug mode on");
        } else {
            log.debug("debug mode off [default]");
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

    // debug: create class and output all settings
    public static void main(String args[]) {
        /*
         * for (int i = 0; i < args.length; i++) {
         * log.debug(">"+args[i]+"<");
         * }
         */
        try {
            CommandLineOptions options = new CommandLineOptions(args);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // options.debug();
    }

}

