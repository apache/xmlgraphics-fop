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

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.logging.Level;
// fop
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.fop.apps.FOPException;

/**
 * CLI_Options handles loading of configuration files and
 * additional setting of commandline options
 */
public class CLI_Options extends UserOptions {

    String[] args = null;

    public CLI_Options(Configuration configuration, String[] args) {
        super(configuration);
        this.args = args;
    }

    /**
     * Configure the system according to the system configuration file
     * config.xml and the user configuration file if it is specified in the
     * system configuration file.
     */
    public void configure(String[] args)
    throws FOPException, FileNotFoundException {
        parseOptions(args);
        super.configure();
    }
    
    private static final boolean TAKES_ARG = true;
    private static final boolean NO_ARG = false;
    private Options makeOptions() {
        // Create the Options object that will be returned
        Options options = new Options();
        OptionBuilder.withArgName("help");
        OptionBuilder.withLongOpt("help");
        OptionBuilder.withDescription("Describe usage and exit");
        options.addOption(OptionBuilder.create("?"));
        // The mutually exclusive verbosity group includes the -d and -q flags
        OptionGroup verbosity = new OptionGroup();
        OptionBuilder.withArgName("debug mode");
        OptionBuilder.withLongOpt("full-error-dump");
        OptionBuilder.withDescription("Verbosity: verbose reporting");
        verbosity.addOption(OptionBuilder.create("d"));
        OptionBuilder.withArgName("quiet mode");
        OptionBuilder.withLongOpt("quiet");
        OptionBuilder.withDescription("Verbosity: report errors only");
        verbosity.addOption(OptionBuilder.create("q"));
        verbosity.setRequired(false);
        // Add verbosity to options
        options.addOptionGroup(verbosity);
        // Add the dump-config option directly
        OptionBuilder.withArgName("dump config");
        OptionBuilder.withLongOpt("dump-config");
        OptionBuilder.withDescription("Dump configuration settings");
        options.addOption(
                OptionBuilder.create("x"));
        // Add the config-file option directly
        OptionBuilder.withArgName("config file");
        OptionBuilder.withLongOpt("config-file");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Configuration file");
        options.addOption(OptionBuilder.create("c"));
        // Add the language option directly
        OptionBuilder.withArgName("language");
        OptionBuilder.withLongOpt("language");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("ISO639 language code");
        options.addOption(OptionBuilder.create("l"));
        // Create the mutually exclusive input group
        OptionGroup input = new OptionGroup();
        OptionBuilder.withArgName("fo:file");
        OptionBuilder.withLongOpt("fo");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Input:XSL-FO input file");
        input.addOption(OptionBuilder.create("fo"));
        OptionBuilder.withArgName("xml file");
        OptionBuilder.withLongOpt("xml");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "Input:XML source file for generating XSL-FO input."
                + " Requires -xsl");
        input.addOption(
                OptionBuilder.create("xml"));
        // Add the input group to the options
        options.addOptionGroup(input);
        // The xsl option depends on the xml input option.  There is no
        // simple way to express this relationship
        OptionBuilder.withArgName("xsl stylesheet");
        OptionBuilder.withLongOpt("xsl");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("XSL stylesheet for transforming XML to XSL-FO");
        options.addOption(OptionBuilder.create("xsl"));
        // Work-around for the xsl parameters
        // Allow multiple arguments (does this apply to multiple instances
        // of the argument specifier?) of the form <name=value>, using '='
        // as a value separator
        OptionBuilder.withArgName("name=value");
        OptionBuilder.withValueSeparator();
        OptionBuilder.withLongOpt("xsl-param");
        OptionBuilder.hasArgs(Option.UNLIMITED_VALUES);
        OptionBuilder.withDescription("Parameter to XSL stylesheet");
        options.addOption(OptionBuilder.create("param"));
        
        // Create the mutually exclusive output group
        OptionGroup output = new OptionGroup();
        OptionBuilder.withArgName("screen renderer");
        OptionBuilder.withLongOpt("awt");
        OptionBuilder.withDescription("Output:Renderered to display using AWT");
        output.addOption(OptionBuilder.create("awt"));
        OptionBuilder.withArgName("pdf output file");
        OptionBuilder.withLongOpt("pdf");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Output:Rendered as PDF to named file");
        output.addOption(OptionBuilder.create("pdf"));
        OptionBuilder.withArgName("postscript output file");
        OptionBuilder.withLongOpt("ps");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Ouput:Rendered as Postscript to named file");
        output.addOption(OptionBuilder.create("ps"));
        OptionBuilder.withArgName("pcl output file");
        OptionBuilder.withLongOpt("pcl");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Output:Rendered as PCL to named file");
        output.addOption(OptionBuilder.create("pcl"));
        OptionBuilder.withArgName("rtf output file");
        OptionBuilder.withLongOpt("rtf");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Output:Rendered as RTF to named file");
        output.addOption(OptionBuilder.create("rtf"));
        OptionBuilder.withArgName("mif output file");
        OptionBuilder.withLongOpt("mif");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Output:Rendered as MIF to named file");
        output.addOption(OptionBuilder.create("mif"));
        OptionBuilder.withArgName("svg output file");
        OptionBuilder.withLongOpt("svg");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Output:Rendered as SVG to named file");
        output.addOption(OptionBuilder.create("svg"));
        OptionBuilder.withArgName("text output file");
        OptionBuilder.withLongOpt("plain-text");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Output:Rendered as plain text to named file");
        output.addOption(OptionBuilder.create("txt"));
        OptionBuilder.withArgName("area tree output file");
        OptionBuilder.withLongOpt("area-tree");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Output:Area tree will be output as XML to named file");
        output.addOption(OptionBuilder.create("at"));
        OptionBuilder.withArgName("help");
        OptionBuilder.withLongOpt("print");
        OptionBuilder.hasOptionalArg();
        OptionBuilder.withDescription("Output:Rendered to the printer. "
                + "Requires extra arguments to the \"java\" command. "
                + "See options with \"-print help\".");
        output.addOption(OptionBuilder.create("print"));
        // Add the ouput group to the options
        options.addOptionGroup(output);
        
        // -s option relevant only to -at area tree output.  Again, no way
        // to express this directly
        OptionBuilder.withArgName("supress low-level areas");
        OptionBuilder.withLongOpt("only-block-areas");
        OptionBuilder.withDescription("Suppress non-block areas in XML renderer");
        options.addOption(OptionBuilder.create("s"));
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
        CommandLineParser parser = new GnuParser();
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
            configuration.put("debugMode", Boolean.TRUE);
            //Fop.setLoggingLevel(Level.FINE);
            log.setLevel(Level.FINE);
        }
        if (cli.hasOption("q")) {
            configuration.put("quiet", Boolean.TRUE);
            //Fop.setLoggingLevel(Level.SEVERE);
            log.setLevel(Level.SEVERE);
        }
        if (cli.hasOption("x")) {
            configuration.put("dumpConfiguration", Boolean.TRUE);
            if (log.getLevel().intValue() > Level.CONFIG.intValue()) {
                //Fop.setLoggingLevel(Level.CONFIG);
                log.setLevel(Level.CONFIG);
            }
        }
        if (cli.hasOption("?")) {
            StringWriter helptext = new StringWriter();
            HelpFormatter help = new HelpFormatter();
            help.printHelp(new PrintWriter(helptext),
                    HelpFormatter.DEFAULT_WIDTH,
                    "Fop", "", options,
                    HelpFormatter.DEFAULT_LEFT_PAD,
                    HelpFormatter.DEFAULT_DESC_PAD,
                    "Any arguments remaining on command line after switches " +
                    "are processed is interpreted as:\n" +
                    "<fo file> <pdf file> if no input or output specified\n"+
                    "or <fo file> if no input specified\n" +
                    "or <pdf file> if no output specified\n\n" +
                    "Verbosity options are mutually exclusive\n" +
                    "Input options are mutually exclusive\n" +
                    "Output options are mutually exclusive");
            log.info(helptext.toString());
            throw new FOPException("Help only");
        }
        if (cli.hasOption("c")) {
            configuration.put("userConfigFileName", cli.getOptionValue("c"));
        }
        if (cli.hasOption("l")) {
            configuration.put("language", cli.getOptionValue("l"));
            //Locale.setDefault(new Locale(cli.getOptionValue("l")));
        }
        if (cli.hasOption("s")) {
            configuration.put("noLowLevelAreas", Boolean.TRUE);
        }
        if (cli.hasOption("fo")) {
            setInputMode(FO_INPUT);
            configuration.put("foFileName", cli.getOptionValue("fo"));
        }
        if (cli.hasOption("xml")) {
            if (cli.hasOption("xsl")) {
                setInputMode(XSLT_INPUT);
                configuration.put("xsltFileName", cli.getOptionValue("xsl"));
            } else {
                throw new FOPException(
                "XSLT file must be specified for the transform mode");
            }
            configuration.put("xmlFileName", cli.getOptionValue("xml"));
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
        
        // Output configuration
        if (cli.hasOption("awt")) {
            setOutputMode(AWT_OUTPUT);
        }
        if (cli.hasOption("pdf")) {
            setOutputMode(PDF_OUTPUT);
            configuration.put("outputFileName", cli.getOptionValue("pdf"));
        }
        if (cli.hasOption("mif")) {
            setOutputMode(MIF_OUTPUT);
            configuration.put("outputFileName", cli.getOptionValue("mif"));
        }
        if (cli.hasOption("rtf")) {
            setOutputMode(RTF_OUTPUT);
            configuration.put("outputFileName", cli.getOptionValue("rtf"));
        }
        if (cli.hasOption("pcl")) {
            setOutputMode(PCL_OUTPUT);
            configuration.put("outputFileName", cli.getOptionValue("pcl"));
        }
        if (cli.hasOption("ps")) {
            setOutputMode(PS_OUTPUT);
            configuration.put("outputFileName", cli.getOptionValue("ps"));
        }
        if (cli.hasOption("txt")) {
            setOutputMode(TXT_OUTPUT);
            configuration.put("outputFileName", cli.getOptionValue("txt"));
        }
        if (cli.hasOption("svg")) {
            setOutputMode(SVG_OUTPUT);
            configuration.put("outputFileName", cli.getOptionValue("svg"));
        }
        if (cli.hasOption("at")) {
            setOutputMode(AREA_OUTPUT);
            configuration.put("outputFileName", cli.getOptionValue("at"));
        }
        if (cli.hasOption("print")) {
            setOutputMode(PRINT_OUTPUT);
            if (cli.getOptionValue("print").toLowerCase(Locale.getDefault())
                    == "help") {
                printUsagePrintOutput();
                throw new FOPException("Print usage only");
            }
        }
        // Get any remaining non-options
        remArgs = cli.getArgs();
        if (remArgs != null) {
            String filename = null;
            int i = 0;
            if (inputmode == NOT_SET && i < remArgs.length
                    && remArgs[i].charAt(0) != '-') {
                setInputMode(FO_INPUT);
                filename = remArgs[i++];
                configuration.put("foFileName", filename);
            }
            if (outputmode == NOT_SET && i < remArgs.length
                    && remArgs[i].charAt(0) != '-') {
                setOutputMode(PDF_OUTPUT);
                configuration.put("outputFileName", remArgs[i++]);
            }
            if (i < remArgs.length) {
                throw new FOPException("Don't know what to do with "
                        + remArgs[i]);
            }
        }
        return true;
    }    // end parseOptions
    
}
