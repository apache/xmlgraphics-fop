/*
 * $Id$
 * 
 *
 * Copyright 1999-2003 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  
 */

package org.apache.fop.apps;

// sax
import org.xml.sax.InputSource;

// java
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
// fop
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationReader;

/**
 * FOPOptions handles loading of configuration files and
 * additional setting of commandline options
 */
public class FOPOptions {

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
    
    private static final int LAST_INPUT_MODE = XSLT_INPUT;
    private static final int LAST_OUTPUT_MODE = RTF_OUTPUT;

    private Configuration configuration = null;

    /* Show debug info. Boolean object set from configuration files.  */
    private boolean debug = false;
    /* show configuration information */
    private boolean dumpConfig = false;
    /* suppress any progress information */
    /* for area tree XML output, only down to block area level */
    /* name of user configuration file */
    private File userConfigFile = null;
    /* name of input fo file */
    private File foFile = null;
    /* name of xsltFile (xslt transformation as input) */
    private File xsltFile = null;
    /* name of xml file (xslt transformation as input) */
    private File xmlFile = null;
    /* name of output file */
    private File outputFile = null;
    /* name of buffer file */
    private File bufferFile = null;
    /* input mode */
    private int inputmode = NOT_SET;
    /* output mode */
    private int outputmode = NOT_SET;
    /* buffer mode */
    private int buffermode = NOT_SET;
    /* language for user information */
    // baseDir (set from the config files
    private String baseDir = null;

    private java.util.HashMap rendererOptions;

    private Logger log;

    private Vector xsltParams = null;
    
    private Options options = new Options();

    private static final String defaultConfigFile = "config.xml";
    private static final String defaultUserConfigFile = "userconfig.xml";
    
    /**
     * An array of String indexed by the integer constants representing
     * the various input modes.  Provided so that integer modes can be
     * mapped to a more descriptive string, and vice versa.
     */
    private String[] inputModes;
    /**
     * An array of String indexed by the integer constants representing
     * the various output modes.  Provided so that integer modes can be
     * mapped to a more descriptive string, and vice versa.
     */
    private String[] outputModes;

    /**
     * Parser variables
     */
    private HashMap arguments = new HashMap();

    /**
     * 
     */
    public FOPOptions(Configuration configuration) {
        setup();
        this.configuration = configuration;
        try {
            configure();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (FOPException e) {
            throw new RuntimeException(e);
        }
    }
    
    public FOPOptions(Configuration configuration, String[] args) {
        setup();
        this.configuration = configuration;
        try {
            configure(args);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (FOPException e) {
            throw new RuntimeException(e);
        }
    }

    private void setup() {
        inputModes = new String[LAST_INPUT_MODE + 1];
        inputModes[NOT_SET] = "NotSet";
        inputModes[FO_INPUT] = "fo";
        inputModes[XSLT_INPUT] = "xslt";
        
        outputModes = new String[LAST_OUTPUT_MODE + 1];
        outputModes[NOT_SET] = "NotSet";
        outputModes[PDF_OUTPUT] = "pdf";
        outputModes[PS_OUTPUT] = "ps";
        outputModes[PCL_OUTPUT] = "pcl";
        outputModes[PRINT_OUTPUT] = "print";
        outputModes[AWT_OUTPUT] = "awt";
        outputModes[MIF_OUTPUT] = "mif";
        outputModes[RTF_OUTPUT] = "rtf";
        outputModes[SVG_OUTPUT] = "svg";
        outputModes[TXT_OUTPUT] = "txt";
        outputModes[AREA_OUTPUT] = "at";
    }

    /**
     * @param mode the mode whose index in the array inputModes is to be
     * returned.
     * @return the int index of the mode string in the array, or -1 if the
     * mode string is not found in the array
     */
    public int inputModeIndex(String mode)
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
    public int outputModeIndex(String mode)
                throws FOPException {
        for (int i = 0; i <= LAST_INPUT_MODE; i++) {
            if (outputModes[i] != null)
                if (mode.equals(outputModes[i]))
                    return i;
        }
        throw new FOPException("Output mode " + mode + " not known");
    }
    
    /**
     * Configure the system according to the system configuration file
     * config.xml and the user configuration file if it is specified in the
     * system configuration file.
     */
    public void configure()
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
    }
    
    public void configure(String[] args)
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
    public int inputModeNameToIndex(String name) {
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
    public int outputModeNameToIndex(String name) {
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
    void loadArguments() {
        String key = null;
        if (arguments != null) {
            Set keys = arguments.keySet();
            Iterator iter = keys.iterator();
            while (iter.hasNext()) {
                key = (String)iter.next();
                configuration.put(key, arguments.get(key));
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
     * @exception FOPException
     */
    void initOptions() throws FOPException {
        String str = null;
        
        // show configuration settings
        dumpConfig = configuration.isTrue("dumpConfiguration");
        
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
        baseDir = configuration.getStringValue("baseDir");
        if (baseDir == null) {
            try {
                baseDir = new File(getInputFile().getAbsolutePath())
                .getParentFile().toURL().toExternalForm();
                configuration.put("baseDir", baseDir);
            } catch (Exception e) {}
        }
        if (debug) {
            Fop.logger.config("base directory: " + baseDir);
        }
        
        if (dumpConfig) {
            configuration.dumpConfiguration();
            System.exit(0);
        }
        
        // quiet mode - this is the last setting, so there is no way to
        // supress the logging of messages during options processing
        if (configuration.isTrue("quiet")) {
            Fop.logger.setLevel(Level.OFF);
        }
        
    }
    
    /**
     * Load the standard configuration file and the user-defined configuration
     * file if one has been defined.  The definition can occur in either the
     * standard file or as a command line argument.
     * @exception FOPException
     */
    private void loadConfigFiles() throws FOPException {
        String str = null;
        loadConfiguration(defaultConfigFile);
        // load user configuration file,if there is one
        // Has the userConfigFile been set from the command line?
        if (arguments != null) {
            if ((str = (String)arguments.get("userConfigFileName")) != null) {
                configuration.put("userConfigFileName", str);
            }
        }
        if ((str = configuration.getStringValue("userConfigFileName"))
        != null) {  // No
            System.out.println("userConfigFileName");
            loadUserConfiguration(str);
        }
    }
    
    /**
     * Convenience class for common functionality required by the config
     * files.
     * @param fname the configuration file name.
     * @param classob the requesting class
     * @return an <tt>InputStream</tt> generated through a call to
     * <tt>getResourceAsStream</tt> on the context <tt>ClassLoader</tt>
     * or the <tt>ClassLoader</tt> for the conf class provided as an argument.
     */
    public InputStream getConfResourceFile(String fname, Class classob)
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
     * @exception FOPException if the configuration file
     * cannot be discovered.
     */
    public void loadConfiguration(String fname)
    throws FOPException {
        InputStream configfile =
            getConfResourceFile(fname, ConfigurationReader.class);
        
        if (debug) {
            Fop.logger.config(
                    "reading configuration file " + fname);
        }
        ConfigurationReader reader = new ConfigurationReader(
                new InputSource(configfile), configuration);
    }
    
    
    /**
     * Load a user-defined configuration file.
     * An initial attempt is made to use a File generated from
     * <code>userConfigFileName</code> as the configuration reader file input
     * source.  If this fails, an attempt is made to load the file using
     * <code>loadConfiguration</code>.
     * @param userConfigFileName the name of the user configuration file.
     */
    public void loadUserConfiguration(String userConfigFileName) {
        // read user configuration file
        boolean readOk = true;
        userConfigFile = new File(userConfigFileName);
        if (userConfigFile == null) {
            return;
        }
        Fop.logger.config(
                "reading user configuration file " + userConfigFileName);
        try {
            ConfigurationReader reader = new ConfigurationReader(
                    InputHandler.fileInputSource(userConfigFile),
                    configuration);
        } catch (FOPException ex) {
            Fop.logger.warning("Can't find user configuration file "
                    + userConfigFile + " in user locations");
            if (debug) {
                ex.printStackTrace();
            }
            readOk = false;
        }
        if (! readOk) {
            try {
                // Try reading the file using loadConfig()
                loadConfiguration(userConfigFileName);
            } catch (FOPException ex) {
                Fop.logger.warning("Can't find user configuration file "
                        + userConfigFile + " in system locations");
                if (debug) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Get the logger.
     * @return the logger
     */
    public Logger getLogger() {
        return log;
    }
    
    private static final boolean TAKES_ARG = true;
    private static final boolean NO_ARG = false;
    private Options makeOptions() {
        // Create the Options object that will be returned
        Options options = new Options();
        // The mutually exclusive verbosity group includes the -d and -q flags
        OptionGroup verbosity = new OptionGroup();
        verbosity.addOption(
                OptionBuilder
                .withArgName("debug mode")
                .withLongOpt("full-error-dump")
                .withDescription("Debug mode: verbose reporting")
                .create("d"));
        verbosity.addOption(
                OptionBuilder
                .withArgName("quiet mode")
                .withLongOpt("quiet")
                .withDescription("Quiet mode: report errors only")
                .create("q"));
        verbosity.setRequired(false);
        // Add verbosity to options
        options.addOptionGroup(verbosity);
        // Add the dump-config option directly
        options.addOption(new Option(
                "x", "dump-config", NO_ARG, "Dump configuration settings"));
        // Add the config-file option directly
        options.addOption(
                OptionBuilder
                .withArgName("config file")
                .withLongOpt("config-file")
                .hasArg()
                .withDescription("Configuration file")
                .create("c"));
        // Add the language option directly
        options.addOption(
                OptionBuilder
                .withArgName("language")
                .withLongOpt("language")
                .hasArg()
                .withDescription("ISO639 language code")
                .create("l"));
        // Create the mutually exclusive input group
        OptionGroup input = new OptionGroup();
        input.addOption(
                OptionBuilder
                .withArgName("fo:file")
                .withLongOpt("fo")
                .hasArg()
                .withDescription("XSL-FO input file")
                .create("fo"));
        input.addOption(
                OptionBuilder
                .withArgName("xml file")
                .withLongOpt("xml")
                .hasArg()
                .withDescription("XML source file for generating XSL-FO input")
                .create("xml"));
        // Add the input group to the options
        options.addOptionGroup(input);
        // The xsl option depends on the xml input option.  There is no
        // simple way to express this relationship
        options.addOption(
                OptionBuilder
                .withArgName("xsl stylesheet")
                .withLongOpt("xsl")
                .hasArg()
                .withDescription("XSL stylesheet for transforming XML to XSL-FO")
                .create("xsl"));
        // Work-around for the xsl parameters
        // Allow multiple arguments (does this apply to multiple instances
        // of the argument specifier?) of the form <name=value>, using '='
        // as a value separator
        options.addOption(
                OptionBuilder
                .withArgName("name=value")
                .withValueSeparator()
                .withLongOpt("xsl-param")
                .hasArgs(Option.UNLIMITED_VALUES)
                .withDescription("Parameter to XSL stylesheet")
                .create("param"));
        
        // Create the mutually exclusive output group
        OptionGroup output = new OptionGroup();
        output.addOption(
                OptionBuilder
                .withLongOpt("awt")
                .withDescription("Input will be renderered to display")
                .create("awt"));
        output.addOption(
                OptionBuilder
                .withArgName("pdf output file")
                .withLongOpt("pdf")
                .hasArg()
                .withDescription("Input will be rendered as PDF to named file")
                .create("pdf"));
        output.addOption(
                OptionBuilder
                .withArgName("postscript output file")
                .withLongOpt("ps")
                .hasArg()
                .withDescription("Input will be rendered as Postscript to named file")
                .create("ps"));
        output.addOption(
                OptionBuilder
                .withArgName("pcl output file")
                .withLongOpt("pcl")
                .hasArg()
                .withDescription("Input will be rendered as PCL to named file")
                .create("pcl"));
        output.addOption(
                OptionBuilder
                .withArgName("rtf output file")
                .withLongOpt("rtf")
                .hasArg()
                .withDescription("Input will be rendered as RTF to named file")
                .create("rtf"));
        output.addOption(
                OptionBuilder
                .withArgName("mif output file")
                .withLongOpt("mif")
                .hasArg()
                .withDescription("Input will be rendered as MIF to named file")
                .create("mif"));
        output.addOption(
                OptionBuilder
                .withArgName("svg output file")
                .withLongOpt("svg")
                .hasArg()
                .withDescription("Input will be rendered as SVG to named file")
                .create("svg"));
        output.addOption(
                OptionBuilder
                .withArgName("text output file")
                .withLongOpt("plain-text")
                .hasArg()
                .withDescription("Input will be rendered as plain text to named file")
                .create("txt"));
        output.addOption(
                OptionBuilder
                .withArgName("area tree output file")
                .withLongOpt("area-tree")
                .hasArg()
                .withDescription("Area tree will be output as XML to named file")
                .create("at"));
        output.addOption(
                OptionBuilder
                .withArgName("help")
                .withLongOpt("print")
                .hasOptionalArg()
                .withDescription("Input will be rendered and sent to the printer. "
                        + "Requires extra arguments to the \"java\" command. "
                        + "See options with \"-print help\".")
                .create("print"));
        
        // -s option relevant only to -at area tree output.  Again, no way
        // to express this directly
        options.addOption(
                OptionBuilder
                .withArgName("supress low-level areas")
                .withLongOpt("only-block-areas")
                .withDescription("Suppress non-block areas in XML renderer")
                .create("s"));
        return options;
    }
    
    private static final boolean STOP_AT_NON_OPTION = true;
    
    /**
     * parses the commandline arguments
     * @return true if parse was successful and processing can continue, false
     * if processing should stop
     * @exception FOPException if there was an error in the format of the options
     */
    private boolean parseOptions(String[] args) throws FOPException {
        options = makeOptions();
        CommandLineParser parser = new PosixParser();
        CommandLine cli;
        String[] xslParams = null;
        String[] remArgs = null;
        try {
            cli = parser.parse(options, args, STOP_AT_NON_OPTION);
        } catch (ParseException e) {
            throw new FOPException(e);
        }
        // Find out what we have
        // Miscellaneous
        if (cli.hasOption("d")) {
            arguments.put("debugMode", Boolean.TRUE);
            //log.setLevel(Level.FINE);
        }
        if (cli.hasOption("q")) {
            arguments.put("quiet", Boolean.TRUE);
            //log.setLevel(Level.SEVERE);
        }
        if (cli.hasOption("x")) {
            arguments.put("dumpConfiguration", Boolean.TRUE);
        }
        if (cli.hasOption("c")) {
            arguments.put("userConfigFileName", cli.getOptionValue("c"));
        }
        if (cli.hasOption("l")) {
            arguments.put("language", cli.getOptionValue("l"));
            //Locale.setDefault(new Locale(cli.getOptionValue("l")));
        }
        if (cli.hasOption("s")) {
            arguments.put("noLowLevelAreas", Boolean.TRUE);
        }
        if (cli.hasOption("fo")) {
            setInputMode(FO_INPUT);
            arguments.put("foFileName", cli.getOptionValue("fo"));
        }
        if (cli.hasOption("xml")) {
            if (cli.hasOption("xsl")) {
                setInputMode(XSLT_INPUT);
                arguments.put("xslFileName", cli.getOptionValue("xsl"));
            } else {
                throw new FOPException(
                "XSLT file must be specified for the transform mode");
            }
            arguments.put("xmlFileName", cli.getOptionValue("xml"));
        } else {
            if (cli.hasOption("xsl")) {
                throw new FOPException(
                "XML file must be specified for the transform mode");
            }
        }
        // Any parameters?
        if (cli.hasOption("param")) {
            // TODO Don't know how to handle these yet
            xslParams = cli.getOptionValues("param");
        }
        
        // Output arguments
        if (cli.hasOption("awt")) {
            setOutputMode(AWT_OUTPUT);
        }
        if (cli.hasOption("pdf")) {
            setOutputMode(PDF_OUTPUT);
            arguments.put("outputFileName", cli.getOptionValue("pdf"));
        }
        if (cli.hasOption("mif")) {
            setOutputMode(MIF_OUTPUT);
            arguments.put("outputFileName", cli.getOptionValue("mif"));
        }
        if (cli.hasOption("rtf")) {
            setOutputMode(RTF_OUTPUT);
            arguments.put("outputFileName", cli.getOptionValue("rtf"));
        }
        if (cli.hasOption("pcl")) {
            setOutputMode(PCL_OUTPUT);
            arguments.put("outputFileName", cli.getOptionValue("pcl"));
        }
        if (cli.hasOption("ps")) {
            setOutputMode(PS_OUTPUT);
            arguments.put("outputFileName", cli.getOptionValue("ps"));
        }
        if (cli.hasOption("txt")) {
            setOutputMode(TXT_OUTPUT);
            arguments.put("outputFileName", cli.getOptionValue("txt"));
        }
        if (cli.hasOption("svg")) {
            setOutputMode(SVG_OUTPUT);
            arguments.put("outputFileName", cli.getOptionValue("svg"));
        }
        if (cli.hasOption("at")) {
            setOutputMode(AREA_OUTPUT);
            arguments.put("outputFileName", cli.getOptionValue("at"));
        }
        if (cli.hasOption("print")) {
            setOutputMode(PRINT_OUTPUT);
            if (cli.getOptionValue("print").toLowerCase(Locale.getDefault())
                    == "help") {
                printUsagePrintOutput();
                throw new FOPException("Usage only");
            }
        }
        // Get any remaining non-options
        remArgs = cli.getArgs();
        if (remArgs != null) {
            int i = 0;
            if (inputmode == NOT_SET && i < remArgs.length
                    && args[i].charAt(0) != '-') {
                setInputMode(FO_INPUT);
                arguments.put("foFileName", remArgs[i++]);
            }
            if (outputmode == NOT_SET && i < remArgs.length
                    && args[i].charAt(0) != '-') {
                setOutputMode(PDF_OUTPUT);
                arguments.put("outputFileName", remArgs[i++]);
            }
            if (i < remArgs.length) {
                throw new FOPException("Don't know what to do with "
                        + remArgs[i]);
            }
        }
        return true;
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
    private void setInputMode(int mode) throws FOPException {
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
    private void setOutputMode(int mode) throws FOPException {
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
            if (!xmlFile.exists()) {
                throw new FileNotFoundException("Error: xml file "
                        + xmlFile.getAbsolutePath()
                        + " not found ");
            }
            if (!xsltFile.exists()) {
                throw new FileNotFoundException("Error: xsl file "
                        + xsltFile.getAbsolutePath()
                        + " not found ");
            }
            
        } else if (inputmode == FO_INPUT) {
            if (!foFile.exists()) {
                throw new FileNotFoundException("Error: fo file "
                        + foFile.getAbsolutePath()
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
                rendererOptions.put("fineDetail", coarseAreaXmlValue());
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
                return new FOFileHandler(foFile);
            case XSLT_INPUT:
                return new XSLTInputHandler(xmlFile, xsltFile, xsltParams);
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
    

    public String getInputMode() {
        return configuration.getStringValue("inputMode");
    }

    /**
     * Returns the input mode (type of input data, ex. NOT_SET or FO_INPUT)
     * @return the input mode
     */
    public int getInputModeIndex() throws FOPException {
        String mode;
        if ((mode = getInputMode()) == null) return NOT_SET;
        return inputModeIndex(mode);
    }

    public String getOutputMode() {
        return configuration.getStringValue("outputMode");
    }

    /**
     * Returns the output mode (output format, ex. NOT_SET or PDF_OUTPUT)
     * @return the output mode
     */
    public int getOutputModeIndex() throws FOPException {
        String mode;
        if ((mode = getOutputMode()) == null) return NOT_SET;
        return outputModeIndex(mode);
    }
    

    public String getFoFileName() {
        return configuration.getStringValue("foFileName");
    }

    public File getFoFile() {
        return foFile;
    }

    public String getXmlFileName() {
        return configuration.getStringValue("xmlFileName");
    }

    public File getXmlFile() {
        return xmlFile;
    }

    public String getXsltFileName() {
        return configuration.getStringValue("xsltFileName");
    }

    public File getXsltFile() {
        return xsltFile;
    }

    public String getOutputFileName() {
        return configuration.getStringValue("outputFileName");
    }

    public File getOutputFile() {
        return outputFile;
    }

    public String getUserConfigFileName() {
        return configuration.getStringValue("userConfigFileName");
    }

    public File getUserConfigFile() {
        return userConfigFile;
    }

    public String getBufferFileName() {
        return configuration.getStringValue("bufferFileName");
    }

    public File getBufferFile() {
        return bufferFile;
    }

    public String getLanguage() {
        return configuration.getStringValue("language");
    }

    public boolean isQuiet() {
        return configuration.isTrue("quiet");
    }

    public Boolean doDumpConfiguration() {
        return configuration.getBooleanObject("dumpConfiguration");
    }

    public boolean isDebugMode() {
        return configuration.isTrue("debugMode");
    }

    public Boolean coarseAreaXmlValue() {
        return configuration.getBooleanObject("noLowLevelAreas");
    }

    public boolean isCoarseAreaXml() {
        return configuration.isTrue("noLowLevelAreas");
    }

    /**
     * return either the foFile or the xmlFile
     */
    public File getInputFile() {
        switch (inputmode) {
        case FO_INPUT:
            return foFile;
        case XSLT_INPUT:
            return xmlFile;
        default:
            return foFile;
        }
    }

    /**
     * shows the commandline syntax including a summary of all available options and some examples
     */
    public void printUsage() {
        HelpFormatter help = new HelpFormatter();
        help.printHelp("FOP", options, true);
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
        log.fine("Input mode: ");
        switch (inputmode) {
            case NOT_SET:
                log.fine("not set");
                break;
            case FO_INPUT:
                log.fine("FO ");
                log.fine("fo input file: " + foFile.toString());
                break;
            case XSLT_INPUT:
                log.fine("xslt transformation");
                log.fine("xml input file: " + xmlFile.toString());
                log.fine("xslt stylesheet: " + xsltFile.toString());
                break;
            default:
                log.fine("unknown input type");
        }
        log.fine("Output mode: ");
        switch (outputmode) {
            case NOT_SET:
                log.fine("not set");
                break;
            case PDF_OUTPUT:
                log.fine("pdf");
                log.fine("output file: " + outputFile.toString());
                break;
            case AWT_OUTPUT:
                log.fine("awt on screen");
                if (outputFile != null) {
                    log.severe("awt mode, but outfile is set:");
                    log.fine("out file: " + outputFile.toString());
                }
                break;
            case MIF_OUTPUT:
                log.fine("mif");
                log.fine("output file: " + outputFile.toString());
                break;
            case RTF_OUTPUT:
                log.fine("rtf");
                log.fine("output file: " + outputFile.toString());
                break;
            case PRINT_OUTPUT:
                log.fine("print directly");
                if (outputFile != null) {
                    log.severe("print mode, but outfile is set:");
                    log.severe("out file: " + outputFile.toString());
                }
                break;
            case PCL_OUTPUT:
                log.fine("pcl");
                log.fine("output file: " + outputFile.toString());
                break;
            case PS_OUTPUT:
                log.fine("PostScript");
                log.fine("output file: " + outputFile.toString());
                break;
            case TXT_OUTPUT:
                log.fine("txt");
                log.fine("output file: " + outputFile.toString());
                break;
            case SVG_OUTPUT:
                log.fine("svg");
                log.fine("output file: " + outputFile.toString());
                break;
            default:
                log.fine("unknown input type");
        }
        
        
        log.fine("OPTIONS");
        if (userConfigFile != null) {
            log.fine("user configuration file: "
                    + userConfigFile.toString());
        } else {
            log.fine("no user configuration file is used [default]");
        }
        if (dumpConfig == true) {
            log.fine("dump configuration");
        } else {
            log.fine("don't dump configuration [default]");
        }
        if (configuration.isTrue("quiet")) {
            log.fine("quiet mode on");
        } else {
            log.fine("quiet mode off [default]");
        }
        
    }
}
