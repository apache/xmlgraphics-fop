/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;

// sax
import org.xml.sax.InputSource;

// java
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

// fop
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationReader;

/**
 * Options handles loading of configuration files and
 * additional setting of commandline options
 */
public class Options {

    /**
     * Render to PDF. OutputStream must be set
     */
    public static final int RENDER_PDF = 1;

    /* input / output not set */
    private static final int NOT_SET = 0;
    /* input: fo file */
    private static final int FO_INPUT = 1;
    /* input: xml+xsl file */
    private static final int XSLT_INPUT = 2;
    private static final int LAST_INPUT_MODE = XSLT_INPUT;
    
    /* output: pdf file */
    private static final int PDF_OUTPUT = 1;
    private static final int LAST_OUTPUT_MODE = PDF_OUTPUT;

    private static final String defaultConfigFile = "config.xml";
    private static final String defaultUserConfigFile = "userconfig.xml";
    /**
     * An array of String indexed by the integer constants representing
     * the various input modes.  Provided so that integer modes can be
     * mapped to a more descriptive string, and vice versa.
     */
    public static final String[] inputModes;
    /**
     * An array of String indexed by the integer constants representing
     * the various output modes.  Provided so that integer modes can be
     * mapped to a more descriptive string, and vice versa.
     */
    public static final String[] outputModes;

    static {
        inputModes = new String[LAST_INPUT_MODE + 1];
        inputModes[NOT_SET] = "NotSet";
        inputModes[FO_INPUT] = "fo";
        inputModes[XSLT_INPUT] = "xslt";

        outputModes = new String[LAST_OUTPUT_MODE + 1];
        outputModes[NOT_SET] = "NotSet";
        outputModes[PDF_OUTPUT] = "pdf";
    }

    /**
     * @param mode the mode whose index in the array inputModes is to be
     * returned.
     * @return the int index of the mode string in the array, or -1 if the
     * mode string is not found in the array
     */
    public static int inputModeIndex(String mode)
                throws FOPException {
        for (int i = 0; i <= LAST_INPUT_MODE; i++) {
            if (inputModes[i] != null)
                if (mode.equals(inputModes[i]))
                    return i;
        }
        throw new FOPException("Input mode " + mode + " not known");
    }

    /**
     * @param mode the mode whose index in the array outputModes is to be
     * returned.
     * @return the int index of the mode string in the array, or -1 if the
     * mode string is not found in the array
     */
    public static int outputModeIndex(String mode)
                throws FOPException {
        for (int i = 0; i <= LAST_INPUT_MODE; i++) {
            if (outputModes[i] != null)
                if (mode.equals(outputModes[i]))
                    return i;
        }
        throw new FOPException("Output mode " + mode + " not known");
    }


    /* Show debug info. Boolean object set from configuration files.  */
    static boolean debug = false;
    /* show configuration information */
    static boolean dumpConfig = false;
    /* suppress any progress information */
    /* for area tree XML output, only down to block area level */
    /* name of user configuration file */
    static File userConfigFile = null;
    /* name of input fo file */
    static File foFile = null;
    /* name of xsltFile (xslt transformation as input) */
    static File xsltFile = null;
    /* name of xml file (xslt transformation as input) */
    static File xmlFile = null;
    /* name of output file */
    static File outputFile = null;
    /* name of buffer file */
    static File bufferFile = null;
    /* input mode */
    static int inputmode = NOT_SET;
    /* output mode */
    static int outputmode = NOT_SET;
    /* buffer mode */
    static int buffermode = NOT_SET;
    /* language for user information */
    // baseDir (set from the config files
    static String baseDir = null;

    /**
     * Parser variables
     */
    private static HashMap arguments = null;

    /**
     * This class cannot be instantiated
     */
    private Options() {}

    /**
     * Configure the system according to the system configuration file
     * config.xml and the user configuration file if it is specified in the
     * system configuration file.
     */
    public static void configure()
            throws FOPException, FileNotFoundException {
        loadConfigFiles();
        loadArguments();
        initOptions();
        try {
            checkSettings();
        } catch (java.io.FileNotFoundException e) {
            printUsage();
            throw e;
        }
        if (debug) debug();
    }

    public static void configure(String[] args)
            throws FOPException, FileNotFoundException {
        parseOptions(args);
        configure();
    }

    /**
     * Method to map an inputMode name to an inputmode index.
     * @param name a String containing the name of an input mode
     * @return the index of that name in the array of input mode names,
     * or -1 if not found
     */
    public static int inputModeNameToIndex(String name) {
        for (int i = 0; i <= LAST_INPUT_MODE; i++) {
            if (name.equals(inputModes[i])) return i;
        }
        return -1;
    }

    /**
     * Method to map an outputMode name to an outputmode index.
     * @param name a String containing the name of an output mode
     * @return the index of that name in the array of output mode names,
     * or -1 if not found
     */
    public static int outputModeNameToIndex(String name) {
        for (int i = 0; i <= LAST_INPUT_MODE; i++) {
            if (name.equals(outputModes[i])) return i;
        }
        return -1;
    }

    /**
     * <code>parseOptions()</code> parses the command line into a
     * <code>HashMap</code> which is
     * passed to this method.  All key-Object pairs are installed in the
     * <code>Configuration</code> maps.
     */
    static void loadArguments() {
        String key = null;
        if (arguments != null) {
            Set keys = arguments.keySet();
            Iterator iter = keys.iterator();
            while (iter.hasNext()) {
                key = (String)iter.next();
                Configuration.put(key, arguments.get(key));
            }
        }
    }


    /**
     * Finish initialization of options.  The command line options, if
     * present, have been parsed and stored in the HashMap arguments.
     * The ints inputmode and outputmode will have been set as a side-
     * effect of command line parsing.
     *
     * The standard configuration file has been read and its contents
     * stored in the Configuration HashMaps.  If a user configuration file
     * was specified in the command line arguments, or, failing that, in
     * the standard configuration file, it had been read and its contents
     * have overridden the Configuration maps.
     *
     * It remains for any related variables defined in this class to be set.
     *
     * @exception org.apache.fop.fo.FOPException
     */
    static void initOptions() throws FOPException {
        Boolean bool = null;
        String str = null;
        // debug mode
        if ((bool = Configuration.getBooleanValue("debugMode")) != null) {
            debug = bool.booleanValue();
        }

        // show configuration settings
        if ((bool = Configuration.getBooleanValue("dumpConfiguration"))
                    != null)
            dumpConfig = bool.booleanValue();

        if ((str = getFoFileName()) != null)
                            foFile = new File(str);
        if ((str = getXmlFileName()) != null)
                            xmlFile = new File(str);
        if ((str = getXsltFileName()) != null)
                            xsltFile = new File(str);
        if ((str = getOutputFileName()) != null)
                            outputFile = new File(str);
        if ((str = getBufferFileName()) != null)
                            bufferFile = new File(str);
        // userConfigFile may be set in the process of loading said file
        if (userConfigFile == null && (str = getUserConfigFileName()) != null)
                            userConfigFile = new File(str);

        if ((str = getInputMode()) != null)
                            inputmode = inputModeIndex(str);
        if ((str = getOutputMode()) != null)
                            outputmode = outputModeIndex(str);

        // set base directory
        // This is not set directly from the command line, but may be set
        // indirectly from the input file setting if not set in the standard
        // or user configuration files
        baseDir = Configuration.getStringValue("baseDir");
        if (baseDir == null) {
            try {
                baseDir = new File(getInputFile().getAbsolutePath())
                                    .getParentFile().toURL().toExternalForm();
                Configuration.put("baseDir", baseDir);
            } catch (Exception e) {}
        }
        if (debug) {
            MessageHandler.logln("base directory: " + baseDir);
        }

        if (dumpConfig) {
            Configuration.dumpConfiguration();
            System.exit(0);
        }

        // quiet mode - this is the last setting, so there is no way to
        // supress the logging of messages during options processing
        if ((bool = isQuiet()) != null) {
            MessageHandler.setQuiet(bool.booleanValue());
        }

    }

    /**
     * Load the standard configuration file and the user-defined configuration
     * file if one has been defined.  The definition can occur in either the
     * standard file or as a command line argument.
     * @exception org.apache.fop.fo.FOPException
     */
    private static void loadConfigFiles() throws FOPException {
        String str = null;
        loadConfiguration(defaultConfigFile);
        // load user configuration file,if there is one
        // Has the userConfigFile been set from the command line?
        if (arguments != null) {
            if ((str = (String)arguments.get("userConfigFileName")) != null) {
                Configuration.put("userConfigFileName", str);
            }
        }
        if ((str = Configuration.getStringValue("userConfigFileName"))
                    != null) {  // No
            System.out.println("userConfigFileName");
            loadUserConfiguration(str);
        }
    }

    /**
     * Convenience class for common functionality required by the config
     * files.
     * @param <tt>Class</tt> object of requesting class.
     * @return an <tt>InputStream</tt> generated through a call to
     * <tt>getResourceAsStream</tt> on the context <tt>ClassLoader</tt>
     * or the <tt>ClassLoader</tt> for the conf class provided as an argument.
     */
    public static InputStream getConfResourceFile(String fname, Class classob)
        throws FOPException
    {
        InputStream configfile = null;

        // Try to use Context Class Loader to load the properties file.
        try {
            java.lang.reflect.Method getCCL =
                Thread.class.getMethod("getContextClassLoader", new Class[0]);
            if (getCCL != null) {
                ClassLoader contextClassLoader =
                    (ClassLoader)getCCL.invoke(Thread.currentThread(),
                                               new Object[0]);
                configfile = contextClassLoader.getResourceAsStream("conf/"
                        + fname);
            }
        } catch (Exception e) {}

        // the entry /conf/config.xml refers to a directory conf
        // which is a sibling of org
        if (configfile == null)
            configfile = classob.getResourceAsStream("/conf/" + fname);
        if (configfile == null) {
            throw new FOPException(
                    "can't find configuration file " + fname);
        }
        return configfile;
    }

    /**
     * Loads configuration file from a system standard place.
     * The context class loader and the <code>ConfigurationReader</code>
     * class loader are asked in turn to <code>getResourceAsStream</code>
     * on <i>fname</i> from a directory called <i>conf</i>.
     * @param fname the name of the configuration file to load.
     * @exception org.apache.fop.fo.FOPException if the configuration file
     * cannot be discovered.
     */
    public static void loadConfiguration(String fname)
        throws FOPException {
        InputStream configfile =
                getConfResourceFile(fname, ConfigurationReader.class);

        if (debug) {
            MessageHandler.logln(
                    "reading configuration file " + fname);
        }
        ConfigurationReader reader =
            new ConfigurationReader(new InputSource(configfile));
        if (debug) {
            reader.setDumpError(true);
        }
        reader.start();
    }


    /**
     * Load a user-defined configuration file.
     * An initial attempt is made to use a File generated from
     * <code>userConfigFileName</code> as the configuration reader file input
     * source.  If this fails, an attempt is made to load the file using
     * <code>loadConfiguration</code>.
     * @param userConfigFileName the name of the user configuration file.
     * @exception org.apache.fop.fo.FOPException thrown when the file cannot
     * be located.
     */
    public static void loadUserConfiguration(String userConfigFileName)
        throws FOPException {
        // read user configuration file
        boolean readOk = true;
        userConfigFile = new File(userConfigFileName);
        if (userConfigFile != null) {
            MessageHandler.logln(
                    "reading user configuration file " + userConfigFileName);
            ConfigurationReader reader = new ConfigurationReader(
                                InputHandler.fileInputSource(userConfigFile));
            if (debug) {
                reader.setDumpError(true);
            }
            try {
                reader.start();
            } catch (org.apache.fop.apps.FOPException error) {
                MessageHandler.logln(
                        "Can't find user configuration file "
                        + userConfigFile + " in user locations");
                if (debug) {
                    reader.dumpError(error);
                }
                readOk = false;
            }
            if (! readOk) {
                // Try reading the file using loadConfig()
                loadConfiguration(userConfigFileName);
            }
        }
    }

    /**
     * parses the commandline arguments into the <code>Hashmap</code>
     * <i>arguments</i>.  Special case processing is done for debug mode,
     * so that debugging output is immediately available.
     * The boolean field <i>debug</i> is
     * set true if the debug flag is passed in the command line args.
     * @exception FOPException if there was an error in the format of the
     * options
     */
    private static void parseOptions(String args[]) throws FOPException {
        arguments = new HashMap(8);
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-d") || args[i].equals("--full-error-dump")) {
                arguments.put("debugMode", new Boolean(true));
                //  SPECIAL CASE
                debug = true;
            } else if (args[i].equals("-x")
                       || args[i].equals("--dump-config")) {
                arguments.put("dumpConfiguration", new Boolean(true));
            } else if (args[i].equals("-q") || args[i].equals("--quiet")) {
                arguments.put("quiet", new Boolean(true));
            } else if (args[i].equals("-c")) {
                if ((i + 1 == args.length)
                        || (args[i + 1].charAt(0) == '-')) {
                    throw new FOPException(
                        "if you use '-c', you must specify the name of the "
                        + "configuration file");
                } else {
                    arguments.put("userConfigFileName", args[++i]);
                }
            } else if (args[i].equals("-l")) {
                if ((i + 1 == args.length)
                        || (args[i + 1].charAt(0) == '-')) {
                    throw new FOPException(
                        "if you use '-l', you must specify a language");
                } else {
                    arguments.put("language", args[++i]);
                }
            } else if (args[i].equals("-s")) {
                arguments.put("noLowLevelAreas", new Boolean(true));
            } else if (args[i].equals("-fo")) {
                setInputMode(FO_INPUT);
                if ((i + 1 == args.length)
                        || (args[i + 1].charAt(0) == '-')) {
                    throw new FOPException(
                        "you must specify the fo file for the '-fo' option");
                } else {
                    arguments.put("foFileName", args[++i]);
                }
            } else if (args[i].equals("-pdf")) {
                setOutputMode(PDF_OUTPUT);
                if ((i + 1 == args.length)
                        || (args[i + 1].charAt(0) == '-')) {
                    throw new FOPException(
                                    "you must specify the pdf output file");
                } else {
                    arguments.put("outputFileName", args[++i]);
                }
            } else if (args[i].charAt(0) != '-') {
                if (inputmode == NOT_SET) {
                    setInputMode(FO_INPUT);
                    arguments.put("foFileName", args[i]);
                } else if (outputmode == NOT_SET) {
                    setOutputMode(PDF_OUTPUT);
                    arguments.put("outputFileName", args[i]);
                } else {
                    throw new FOPException("Don't know what to do with "
                                           + args[i]);
                }
            } else {
                throw new FOPException("Don't know what to do with "
                                       + args[i]);
            }
        }
    }    // end parseOptions

    /**
     * If the <code>String</code> value for the key <code>inputMode</code>
     * has not been installed in <code>Configuration</code>, install the
     * value passed in the parameter, and set the field <code>inputmode</code>
     * to the integer value associated with <code>mode</code>.
     * If the key already exists with the same value as <code>mode</code>,
     * do nothing.
     * If the key already exists with a different value to <code>mode</code>,
     * throw an exception.
     * @param mode the input mode code
     * @exception FOPException
     */
    private static void setInputMode(int mode) throws FOPException {
        String tempMode = null;
        if ((tempMode = getInputMode()) == null) {
            arguments.put("inputMode", inputModes[mode]);
            inputmode = mode;
        } else if (tempMode.equals(inputModes[mode])) {
            return;
        } else {
            throw new FOPException("you can only set one input method");
        }
    }

    /**
     * If the <code>String</code> value for the key <code>outputMode</code>
     * has not been installed in <code>Configuration</code>, install the
     * value passed in the parameter, and set the field <code>outputmode</code>
     * to the integer value associated with <code>mode</code>.
     * If the key already exists with the same value as <code>mode</code>,
     * do nothing.
     * If the key already exists with a different value to <code>mode</code>,
     * throw an exception.
     * @param mode the output mode code
     * @exception FOPException
     */
    private static void setOutputMode(int mode) throws FOPException {
        String tempMode = null;
        if ((tempMode = getOutputMode()) == null) {
            arguments.put("outputMode", outputModes[mode]);
            outputmode = mode;
        } else if (tempMode.equals(outputModes[mode])) {
            return;
        } else {
            throw new FOPException("you can only set one output method");
        }
    }

    /**
     * checks whether all necessary information has been given in a
     * consistent way
     */
    private static void checkSettings()
                throws FOPException, FileNotFoundException {
        if (inputmode == NOT_SET) {
            throw new FOPException("No input file specified");
        }

        if (outputmode == NOT_SET) {
            throw new FOPException("No output file specified");
        }

        if (inputmode == XSLT_INPUT) {
            // check whether xml *and* xslt file have been set
            if (xmlFile == null) {
                throw new FOPException(
                        "XML file must be specified for the tranform mode");
            }
            if (xsltFile == null) {
                throw new FOPException(
                        "XSLT file must be specified for the tranform mode");
            }

            // warning if foFile has been set in xslt mode
            if (foFile != null) {
                MessageHandler.errorln(
                    "WARNING: "
                    + "Can't use fo file with transform mode! Ignoring.\n"
                                       + "Your input is " + "\n xmlFile: "
                                       + xmlFile.getAbsolutePath()
                                       + "\nxsltFile: "
                                       + xsltFile.getAbsolutePath()
                                       + "\n  foFile: "
                                       + foFile.getAbsolutePath());
            }
            if (!xmlFile.exists()) {
                throw new FileNotFoundException("xml file "
                                                + xmlFile.getAbsolutePath()
                                                + " not found ");
            }
            if (!xsltFile.exists()) {
                throw new FileNotFoundException("xsl file "
                                                + xsltFile.getAbsolutePath()
                                                + " not found ");
            }

        } else if (inputmode == FO_INPUT) {
            if (xmlFile != null || xsltFile != null) {
                MessageHandler.errorln(
                    "WARNING: fo input mode, but xmlFile or xslt file are set:"
                );
                MessageHandler.errorln("xml file: " + xmlFile.toString());
                MessageHandler.errorln("xslt file: " + xsltFile.toString());
            }
            if (!foFile.exists()) {
                throw new FileNotFoundException("fo file "
                                                + foFile.getAbsolutePath()
                                                + " not found ");
            }

        }
    }    // end checkSettings

    /**
     * returns the chosen renderer, throws FOPException
     */
    public static int getRenderer() throws FOPException {
        switch (outputmode) {
        case NOT_SET:
            throw new FOPException("Renderer has not been set!");
        case PDF_OUTPUT:
            return RENDER_PDF;
        default:
            throw new FOPException("Invalid Renderer setting!");
        }
    }

    /**
     *
     */
    public static InputHandler getInputHandler() {
        switch (inputmode) {
        case FO_INPUT:
            return new FOInputHandler(foFile);
        default:
            return new FOInputHandler(foFile);
        }
    }

    public static CommandLineStarter getStarter() throws FOPException {
        switch (outputmode) {

        default:
            return new CommandLineStarter();
        }
    }

    public static String getInputMode() {
        return Configuration.getStringValue("inputMode");
    }

    public static int getInputModeIndex() throws FOPException {
        String mode;
        if ((mode = getInputMode()) == null) return NOT_SET;
        return inputModeIndex(mode);
    }

    public static String getOutputMode() {
        return Configuration.getStringValue("outputMode");
    }

    public static int getOutputModeOutdex() throws FOPException {
        String mode;
        if ((mode = getOutputMode()) == null) return NOT_SET;
        return outputModeIndex(mode);
    }

    public static String getFoFileName() {
        return Configuration.getStringValue("foFileName");
    }

    public static File getFoFile() {
        return foFile;
    }

    public static String getXmlFileName() {
        return Configuration.getStringValue("xmlFileName");
    }

    public static File getXmlFile() {
        return xmlFile;
    }

    public static String getXsltFileName() {
        return Configuration.getStringValue("xsltFileName");
    }

    public static File getXsltFile() {
        return xsltFile;
    }

    public static String getOutputFileName() {
        return Configuration.getStringValue("outputFileName");
    }

    public static File getOutputFile() {
        return outputFile;
    }

    public static String getUserConfigFileName() {
        return Configuration.getStringValue("userConfigFileName");
    }

    public static File getUserConfigFile() {
        return userConfigFile;
    }

    public static String getBufferFileName() {
        return Configuration.getStringValue("bufferFileName");
    }

    public static File getBufferFile() {
        return bufferFile;
    }

    public static String getLanguage() {
        return Configuration.getStringValue("language");
    }

    public static Boolean isQuiet() {
        return Configuration.getBooleanValue("quiet");
    }

    public static Boolean doDumpConfiguration() {
        return Configuration.getBooleanValue("dumpConfiguration");
    }

    public static Boolean isDebugMode() {
        return Configuration.getBooleanValue("debugMode");
    }

    public static Boolean isCoarseAreaXml() {
        return Configuration.getBooleanValue("noLowLevelAreas");
    }

    /**
     * return either the foFile or the xmlFile
     */
    public static File getInputFile() {
        switch (inputmode) {
        case FO_INPUT:
            return foFile;
        default:
            return foFile;
        }
    }

    /**
     * shows the commandline syntax including a summary of all available
     * options and some examples
     */
    public static void printUsage() {
        MessageHandler.errorln(
            "\nUSAGE\n"
            + "Fop [options] [-fo|-xml] infile [-xsl file] "
            + "[-awt|-pdf|-mif|-pcl|-ps|-txt|-at|-print] [outputFile]\n"
            + " [OPTIONS]  \n"
            + "  -d       debug mode   \n"
            + "  -x       dump configuration settings  \n"
            + "  -q       quiet mode  \n"
            + "  -c cfg.xml  use additional configuration file cfg.xml\n"
            + "  -l lang     the language to use for user information \n"
            + "  -s       for area tree XML, down to block areas only\n\n"
            + " [INPUT]  \n"
            + "  infile         xsl:fo input file (the same as the next) \n"
            + "  -fo  infile    xsl:fo input file  \n"
            + "  -xml infile       "
            + "xml input file, must be used together with -xsl \n"
            + "  -xsl stylesheet   xslt stylesheet \n \n"
            + " [OUTPUT] \n"
            + "  outputFile        "
            + "input will be rendered as pdf file into outputFile \n"
            + "  -pdf outputFile   "
            + "input will be rendered as pdf file (outputFile req'd) \n"
            + "  -awt           "
            + "input will be displayed on screen \n"
            + "  -mif outputFile   "
            + "input will be rendered as mif file (outputFile req'd)\n"
            + "  -pcl outputFile   "
            + "input will be rendered as pcl file (outputFile req'd) \n"
            + "  -ps outputFile    "
            + "input will be rendered as PostScript file (outputFile req'd) \n"
            + "  -txt outputFile   "
            + "input will be rendered as text file (outputFile req'd) \n"
            + "  -at outputFile    "
            + "representation of area tree as XML (outputFile req'd) \n"
            + "  -print         "
            + "input file will be rendered and sent to the printer \n"
            + "                 see options with \"-print help\" \n\n"
            + " [Examples]\n" + "  Fop foo.fo foo.pdf \n"
            + "  Fop -fo foo.fo -pdf foo.pdf "
            + "(does the same as the previous line)\n"
            + "  Fop -xsl foo.xsl -xml foo.xml -pdf foo.pdf\n"
            + "  Fop foo.fo -mif foo.mif\n"
            + "  Fop foo.fo -print or Fop -print foo.fo \n"
            + "  Fop foo.fo -awt \n");
    }

    /**
     * shows the options for print output
     */
    public static void printUsagePrintOutput() {
        MessageHandler.errorln(
            "USAGE:"
            + " -print [-Dstart=i] [-Dend=i] [-Dcopies=i] [-Deven=true|false]"
            + " org.apache.fop.apps.Fop (..) -print \n"
            + "Example:\n"
            + "java -Dstart=1 -Dend=2 org.apache.Fop.apps.Fop infile.fo -print"
        );
    }


    /**
     * debug mode. outputs all commandline settings
     */
    private static void debug() {
        Boolean bool;
        System.out.println("Version: "
                                + Configuration.getStringValue("version"));
        System.out.print("Input mode: ");
        switch (inputmode) {
        case NOT_SET:
            MessageHandler.logln("not set");
            break;
        case FO_INPUT:
            MessageHandler.logln("fo ");
            MessageHandler.logln("fo input file: " + foFile.toString());
            break;
        default:
            MessageHandler.logln("unknown input type");
        }
        System.out.print("Output mode: ");
        switch (outputmode) {
        case NOT_SET:
            MessageHandler.logln("not set");
            break;
        case PDF_OUTPUT:
            MessageHandler.logln("pdf");
            MessageHandler.logln("output file: " + outputFile.toString());
            break;
        default:
            MessageHandler.logln("unknown input type");
        }


        MessageHandler.logln("OPTIONS");
        if (userConfigFile != null) {
            MessageHandler.logln("user configuration file: "
                                 + userConfigFile.toString());
        } else {
            MessageHandler.logln(
                            "no user configuration file is used [default]");
        }
        if ((bool = isDebugMode()) != null && bool.booleanValue()) {
            MessageHandler.logln("debug mode on");
        } else {
            MessageHandler.logln("debug mode off [default]");
        }
        if ((bool = doDumpConfiguration()) != null && bool.booleanValue()) {
            MessageHandler.logln("dump configuration");
        } else {
            MessageHandler.logln("don't dump configuration [default]");
        }
        if ((bool = isCoarseAreaXml()) != null && bool.booleanValue()) {
            MessageHandler.logln("no low level areas");
        } else {
            MessageHandler.logln("low level areas generated[default]");
        }
        if ((bool = isQuiet()) != null && bool.booleanValue()) {
            MessageHandler.logln("quiet mode on");
        } else {
            MessageHandler.logln("quiet mode off [default]");
        }

    }

}
