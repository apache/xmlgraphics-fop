/*
 *
 * Copyright 1999-2004 The Apache Software Foundation.
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
 * $Id$
 */

package org.apache.fop.configuration;

// sax
import org.xml.sax.InputSource;

// java
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FOFileHandler;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.InputHandler;
import org.apache.fop.apps.XSLTInputHandler;

/**
 * SystemOptions handles loading of configuration files and
 * additional setting of commandline options
 */
public class SystemOptions {

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
    
    protected static final int LAST_INPUT_MODE = XSLT_INPUT;
    protected static final int LAST_OUTPUT_MODE = RTF_OUTPUT;

    protected Configuration configuration = null;

//    /* show configuration information */
//    protected boolean dumpConfig = false;
    /* input mode */
    protected int inputmode = NOT_SET;
    /* output mode */
    protected int outputmode = NOT_SET;
    // baseDir (set from the config files
    protected String baseDir = null;

    protected java.util.HashMap rendererOptions;

    protected Logger log = Logger.getLogger(Fop.fopPackage);

    protected Vector xsltParams = null;
    
    protected Options options = new Options();

    protected static final String defaultConfigFile = "config.xml";
    
    /**
     * An array of String indexed by the integer constants representing
     * the various input modes.  Provided so that integer modes can be
     * mapped to a more descriptive string, and vice versa.
     */
    protected String[] inputModes;
    /**
     * An array of String indexed by the integer constants representing
     * the various output modes.  Provided so that integer modes can be
     * mapped to a more descriptive string, and vice versa.
     */
    protected String[] outputModes;


    /**
     * 
     */
    public SystemOptions(Configuration configuration) {
        setup();
        this.configuration = configuration;
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
        loadConfiguration(getSystemConfigFileName());
        initOptions();
        try {
            checkSettings();
        } catch (java.io.FileNotFoundException e) {
            printUsage();
            throw e;
        }
    }
    
    /**
     * Method to map an inputMode name to an inputmode index.
     * @param name a String containing the name of an input mode
     * @return the index of that name in the array of input mode names,
     * or -1 if not found
     */
    protected int inputModeNameToIndex(String name) {
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
    protected int outputModeNameToIndex(String name) {
        for (int i = 0; i <= LAST_INPUT_MODE; i++) {
            if (name.equals(outputModes[i])) return i;
        }
        return -1;
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
    protected void initOptions() throws FOPException {
        String str = null;
        
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
        if (isDebugMode()) {
            log.config("base directory: " + baseDir);
        }
        
        if (dumpConfig()) {
            configuration.dumpConfiguration();
            System.exit(0);
        }
        
        // quiet mode - this is the last setting, so there is no way to
        // supress the logging of messages during options processing
        if (configuration.isTrue("quiet")) {
            log.setLevel(Level.OFF);
        }
        
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
    protected void loadConfiguration(String fname)
    throws FOPException {
        InputStream configfile = ConfigurationResource.getResourceFile(
                "conf/" + fname, ConfigurationReader.class);
        
        if (isDebugMode()) {
            log.config(
                    "reading configuration file conf/" + fname);
        }
        ConfigurationReader reader = new ConfigurationReader(
                new InputSource(configfile), configuration);
    }
    
    
    /**
     * Get the log.
     * @return the log
     */
    protected Logger getLogger() {
        return log;
    }
    

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
    protected void setInputMode(int mode)
    throws FOPException {
        String tempMode = null;
        if ((tempMode = getInputMode()) == null) {
            configuration.put("inputMode", inputModes[mode]);
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
    protected void setOutputMode(int mode)
    throws FOPException {
        String tempMode = null;
        if ((tempMode = getOutputMode()) == null) {
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
    protected void checkSettings() throws FOPException, FileNotFoundException {
        if (inputmode == NOT_SET) {
            throw new FOPException("No input file specified");
        }
        
        if (outputmode == NOT_SET) {
            throw new FOPException("No output file specified");
        }
        
        if (inputmode == XSLT_INPUT) {
            if (getXmlFile() == null) {
                throw new FOPException("No xml input file specified");
            }
            if (!getXmlFile().exists()) {
                throw new FileNotFoundException("Error: xml file "
                        + getXmlFile().getAbsolutePath()
                        + " not found ");
            }
            if (getXsltFile() == null ) {
                throw new FOPException(
                        "No xslt transformation file specified");
            }
            if (!getXsltFile().exists()) {
                throw new FileNotFoundException("Error: xsl file "
                        + getXsltFile().getAbsolutePath()
                        + " not found ");
            }
            
        } else if (inputmode == FO_INPUT) {
            if (getFoFile() == null) {
                throw new FOPException("No fo input file specified");
            }
            if (!getFoFile().exists()) {
                throw new FileNotFoundException("Error: fo file "
                        + getFoFile().getAbsolutePath()
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
                return Fop.RENDER_PDF;
            case AWT_OUTPUT:
                return Fop.RENDER_AWT;
            case MIF_OUTPUT:
                return Fop.RENDER_MIF;
            case PRINT_OUTPUT:
                return Fop.RENDER_PRINT;
            case PCL_OUTPUT:
                return Fop.RENDER_PCL;
            case PS_OUTPUT:
                return Fop.RENDER_PS;
            case TXT_OUTPUT:
                return Fop.RENDER_TXT;
            case SVG_OUTPUT:
                return Fop.RENDER_SVG;
            case AREA_OUTPUT:
                rendererOptions.put("fineDetail", coarseAreaXmlValue());
                return Fop.RENDER_XML;
            case RTF_OUTPUT:
                return Fop.RENDER_RTF;
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
                return new FOFileHandler(getFoFile());
            case XSLT_INPUT:
                return new XSLTInputHandler(
                        getXmlFile(), getXsltFile(), xsltParams);
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
    protected int getInputModeIndex() throws FOPException {
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
    protected int getOutputModeIndex() throws FOPException {
        String mode;
        if ((mode = getOutputMode()) == null) return NOT_SET;
        return outputModeIndex(mode);
    }
    

    public String getFoFileName() {
        return configuration.getStringValue("foFileName");
    }

    public File getFoFile() {
        String fname;
        if ((fname = getFoFileName()) == null) {
            return null;
        }
        return new File(fname);
    }

    public String getXmlFileName() {
        return configuration.getStringValue("xmlFileName");
    }

    public File getXmlFile() {
        String fname;
        if ((fname = getXmlFileName()) == null) {
            return null;
        }
        return new File(fname);
    }

    public String getXsltFileName() {
        return configuration.getStringValue("xsltFileName");
    }

    public File getXsltFile() {
        String fname;
        if ((fname = getXsltFileName()) == null) {
            return null;
        }
        return new File(fname);
    }

    public String getOutputFileName() {
        return configuration.getStringValue("outputFileName");
    }

    public File getOutputFile() {
        String fname;
        if ((fname = getOutputFileName()) == null) {
            return null;
        }
        return new File(fname);
    }

    public String getSystemConfigFileName() {
        String nameFromConfig = null;
        if ((nameFromConfig = configuration.getStringValue("configFileName"))
        != null) {
            return nameFromConfig;
        }
        return defaultConfigFile;
    }

    public File getSystemConfigFile() {
        String fname;
        if ((fname = getSystemConfigFileName()) == null) {
            return null;
        }
        return new File(fname);
    }

    public String getUserConfigFileName() {
        return configuration.getStringValue("userConfigFileName");
    }

    public File getUserConfigFile() {
        String fname;
        if ((fname = getUserConfigFileName()) == null) {
            return null;
        }
        return new File(fname);
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

    public boolean dumpConfig() {
        return doDumpConfiguration().booleanValue();
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
            return getFoFile();
        case XSLT_INPUT:
            return getXmlFile();
        default:
            return getFoFile();
        }
    }

    /**
     * shows the commandline syntax including a summary of all available options and some examples
     */
    protected void printUsage() {
        HelpFormatter help = new HelpFormatter();
        help.printHelp("FOP", options, true);
    }
    
    /**
     * shows the options for print output
     */
    protected void printUsagePrintOutput() {
        System.err.println("USAGE: -print [-Dstart=i] [-Dend=i] [-Dcopies=i] [-Deven=true|false] "
                + " org.apache.fop.apps.Fop (..) -print \n"
                + "Example:\n"
                + "java -Dstart=1 -Dend=2 org.apache.Fop.apps.Fop infile.fo -print ");
    }
    
    
    /**
     * debug mode. outputs all commandline settings
     */
    protected void debug() {
        StringBuffer fine = new StringBuffer();
        StringBuffer severe = new StringBuffer();
        fine.append("Input mode: ");
        switch (inputmode) {
            case NOT_SET:
                fine.append("not set");
                break;
            case FO_INPUT:
                fine.append("FO ");
                fine.append("fo input file: " + getFoFileName());
                break;
            case XSLT_INPUT:
                fine.append("xslt transformation");
                fine.append("xml input file: " + getXmlFileName());
                fine.append("xslt stylesheet: " + getXsltFileName());
                break;
            default:
                fine.append("unknown input type");
        }
        fine.append("\nOutput mode: ");
        switch (outputmode) {
            case NOT_SET:
                fine.append("not set");
                break;
            case PDF_OUTPUT:
                fine.append("pdf");
                fine.append("output file: " + getOutputFileName());
                break;
            case AWT_OUTPUT:
                fine.append("awt on screen");
                if (getOutputFile() != null) {
                    severe.append("awt mode, but outfile is set:\n");
                    severe.append("output file: " + getOutputFileName() + "\n");
                }
                break;
            case MIF_OUTPUT:
                fine.append("mif");
                fine.append("output file: " + getOutputFileName());
                break;
            case RTF_OUTPUT:
                fine.append("rtf");
                fine.append("output file: " + getOutputFileName());
                break;
            case PRINT_OUTPUT:
                fine.append("print directly");
                if (getOutputFile() != null) {
                    severe.append("print mode, but outfile is set:\n");
                    severe.append("output file: " + getOutputFileName() + "\n");
                }
                break;
            case PCL_OUTPUT:
                fine.append("pcl");
                fine.append("output file: " + getOutputFileName());
                break;
            case PS_OUTPUT:
                fine.append("PostScript");
                fine.append("output file: " + getOutputFileName());
                break;
            case TXT_OUTPUT:
                fine.append("txt");
                fine.append("output file: " + getOutputFileName());
                break;
            case SVG_OUTPUT:
                fine.append("svg");
                fine.append("output file: " + getOutputFileName());
                break;
            default:
                fine.append("unknown input type");
        }
        
        
        fine.append("\nOPTIONS\n");
        if (getUserConfigFileName() != null) {
            fine.append("user configuration file: "
                    + getUserConfigFileName());
        } else {
            fine.append("no user configuration file is used [default]");
        }
        fine.append("\n");
        if (dumpConfig()) {
            fine.append("dump configuration");
        } else {
            fine.append("don't dump configuration [default]");
        }
        fine.append("\n");
        if (configuration.isTrue("quiet")) {
            fine.append("quiet mode on");
        } else {
            fine.append("quiet mode off [default]");
        }
        fine.append("\n");
        log.fine(fine.toString());
        if (severe.toString() != "") {
            log.severe(severe.toString());
        }
        
    }
}
