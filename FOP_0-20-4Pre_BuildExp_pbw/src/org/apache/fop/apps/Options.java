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
import java.io.InputStream;

// fop
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationReader;

/**
 * Options handles loading of configuration files and
 * additional setting of commandline options
 */
public class Options {
    boolean errorDump = false;

    public Options() throws FOPException {
        this.loadStandardConfiguration();
        initOptions();
    }

    public Options(File userConfigFile) throws FOPException {
        this();
        this.loadUserconfiguration(userConfigFile);
    }

    public Options(CommandLineOptions clOptions) throws FOPException {
        this();
        this.setCommandLineOptions(clOptions);
    }

    // initializing option settings
    void initOptions() {
        if (Configuration.getBooleanValue("quiet").booleanValue()) {
            //MessageHandler.setQuiet(true);
        }
        if (Configuration.getBooleanValue("debugMode").booleanValue()) {
            errorDump = true;
        }
        if (Configuration.getBooleanValue("dumpConfiguration").booleanValue()) {
            Configuration.put("dumpConfiguration", "true");
            Configuration.dumpConfiguration();
        }
    }

    // setting clOptions
    void setCommandLineOptions(CommandLineOptions clOptions) {
        // load user configuration file,if there is one
        File userConfigFile = clOptions.getUserConfigFile();
        if (userConfigFile != null) {
            this.loadUserconfiguration(userConfigFile);
        }

        // debug mode
        if (clOptions.isDebugMode() != null) {
            errorDump = clOptions.isDebugMode().booleanValue();
            Configuration.put("debugMode", new Boolean(errorDump));
        }

        // show configuration settings
        boolean dumpConfiguration;
        if (clOptions.dumpConfiguration() != null) {
            dumpConfiguration = clOptions.dumpConfiguration().booleanValue();
        } else {
            dumpConfiguration =
                Configuration.getBooleanValue("dumpConfiguration").booleanValue();
        }
        if (dumpConfiguration) {
            Configuration.put("dumpConfiguration", "true");
            Configuration.dumpConfiguration();
        }

        // quiet mode
        if (clOptions.isQuiet() != null) {
            //MessageHandler.setQuiet(clOptions.isQuiet().booleanValue());
        }

        // set base directory
        String baseDir = Configuration.getStringValue("baseDir");
        if (baseDir == null) {
            try {
                baseDir =
                    new File(clOptions.getInputFile().getAbsolutePath()).getParentFile().toURL().toExternalForm();
                Configuration.put("baseDir", baseDir);
            } catch (Exception e) {}
        }
        if (errorDump) {
            //log.debug("base directory: " + baseDir);
        }
    }

    /**
     * loads standard configuration file and a user file, if it has been specified
     */
    public void loadStandardConfiguration() throws FOPException {
        String file = "config.xml";
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
                        + file);
            }
        } catch (Exception e) {}

        // the entry /conf/config.xml refers to a directory conf which is a sibling of org
        if (configfile == null)
            configfile =
                ConfigurationReader.class.getResourceAsStream("/conf/"
                    + file);
        if (configfile == null) {
            throw new FOPException("can't find default configuration file");
        }
        if (errorDump) {
            //log.error("reading default configuration file");
        }
        ConfigurationReader reader =
            new ConfigurationReader(new InputSource(configfile));
        if (errorDump) {
            reader.setDumpError(true);
        }
        reader.start();

    }

    public void loadUserconfiguration(String userConfigFile) {
        loadUserconfiguration(new File(userConfigFile));
    }

    public void loadUserconfiguration(File userConfigFile) {
        // read user configuration file
        if (userConfigFile != null) {
            //log.debug("reading user configuration file");
            ConfigurationReader reader =
                new ConfigurationReader(InputHandler.fileInputSource(userConfigFile));
            if (errorDump) {
                reader.setDumpError(true);
            }
            try {
                reader.start();
            } catch (org.apache.fop.apps.FOPException error) {
                //log.error("Could not load user configuration file "
                //                       + userConfigFile + " - error: "
                //                       + error.getMessage());
                //log.error("using default values");
                if (errorDump) {
                    reader.dumpError(error);
                }
            }
        }
    }

}


