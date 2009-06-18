/*
 * $Id$
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

// SAX
import org.xml.sax.InputSource;

// Java
import java.io.File;
import java.io.InputStream;

// FOP
import org.apache.fop.messaging.MessageHandler;
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

    public Options(InputStream userConfig) throws FOPException {
        this();
        this.loadUserconfiguration(userConfig);
    }

    public Options(InputSource userConfig) throws FOPException {
        this();
        this.loadUserconfiguration(userConfig);
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
            MessageHandler.setQuiet(true);
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
            System.exit(0);
        }

        // quiet mode
        if (clOptions.isQuiet() != null) {
            MessageHandler.setQuiet(clOptions.isQuiet().booleanValue());
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
            MessageHandler.logln("base directory: " + baseDir);
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
            MessageHandler.logln("reading default configuration file");
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
        if (userConfigFile != null) {
            loadUserconfiguration(InputHandler.fileInputSource(userConfigFile));
        }
    }

    public void loadUserconfiguration(InputStream userConfig) {
        loadUserconfiguration(new InputSource(userConfig));
    }

    public void loadUserconfiguration(InputSource userConfigSource) {
        // read user configuration
        ConfigurationReader reader =
            new ConfigurationReader(userConfigSource);
        if (errorDump) {
            reader.setDumpError(true);
        }
        try {
            reader.start();
        } catch (org.apache.fop.apps.FOPException error) {
            MessageHandler.errorln("Could not load user configuration "
                                   + userConfigSource.getSystemId() + " - error: "
                                   + error.getMessage());
            MessageHandler.errorln("using default values");
            if (errorDump) {
                reader.dumpError(error);
            }
        }
    }

}


