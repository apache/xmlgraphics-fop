/* 
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the 
 * LICENSE file included with these sources."
 */


package org.apache.fop.apps;

//sax
import org.xml.sax.InputSource;

//java
import java.io.File;
import java.io.InputStream;

//fop
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationReader;
	
/**
 *	Options handles loading of configuration files and 
 *  additional setting of commandline options
 */

public class Options {
	boolean errorDump = false;
	
	public Options () {
		this.loadStandardConfiguration();
		initOptions ();
	}
	
	public Options (File userConfigFile) {
		this();
		this.loadUserconfiguration(userConfigFile);
	}

	public Options (CommandLineOptions clOptions) {
		this();
		this.setCommandLineOptions(clOptions);
	}

	//initializing option settings	
	void initOptions () {
		if (Configuration.getBooleanValue("quiet").booleanValue()) {
			MessageHandler.setQuiet(true);		
		}
		if (Configuration.getBooleanValue("debugMode").booleanValue()) {
			errorDump = true;
		}
        if (Configuration.getBooleanValue("dumpConfiguration").booleanValue()) {		
			Configuration.put("dumpConfiguration","true");			
			Configuration.dumpConfiguration();
		}
	}
	
	//setting clOptions
    void setCommandLineOptions(CommandLineOptions clOptions) {
		//load user configuration file,if there is one
		File userConfigFile = clOptions.getUserConfigFile();
        if (userConfigFile != null) {
            this.loadUserconfiguration(userConfigFile);
        }
        
        //debug mode
		if (clOptions.isDebugMode() != null) {
			errorDump = clOptions.isDebugMode().booleanValue();
			Configuration.put("debugMode",new Boolean(errorDump));			
		} 
		
		//show configuration settings
		boolean dumpConfiguration;
		if (clOptions.dumpConfiguration() != null) {
			dumpConfiguration = clOptions.dumpConfiguration().booleanValue();
		} else {
			dumpConfiguration = Configuration.getBooleanValue("dumpConfiguration").booleanValue();
		}
        if (dumpConfiguration) {		
			Configuration.put("dumpConfiguration","true");			
			Configuration.dumpConfiguration();
            System.exit(0);
		}
		
		//quiet mode
        if (clOptions.isQuiet() != null) {
            MessageHandler.setQuiet(clOptions.isQuiet().booleanValue());
		} 
		
		//set base directory
        String baseDir = Configuration.getStringValue("baseDir");
        if (baseDir == null) {
            baseDir = new File(clOptions.getInputFile().getAbsolutePath()).getParent();
            Configuration.put("baseDir",baseDir);
        }
        if (errorDump) {
            MessageHandler.logln("base directory: " + baseDir);
        }
    }

    /**
        *  loads standard configuration file and a user file, if it has been specified
        */
    public void loadStandardConfiguration() {
        String file = "config.xml";

        // the entry /conf/config.xml refers to a directory conf which is a sibling of org
        InputStream configfile =
          ConfigurationReader.class.getResourceAsStream("/conf/"+
                  file);
        if (configfile == null) {
            MessageHandler.errorln("Fatal error: can't find default configuration file");
            System.exit(1);
        }
        if (errorDump) {
            MessageHandler.logln("reading default configuration file");
        }
        ConfigurationReader reader =
          new ConfigurationReader (new InputSource(configfile));
        if (errorDump) {
            reader.setDumpError(true);
        }
        try {
            reader.start();
        } catch (org.apache.fop.apps.FOPException error) {
            MessageHandler.errorln("Fatal Error: Can't process default configuration file. \nProbably it is not well-formed.");
            if (errorDump) {
                reader.dumpError(error);
            }
            System.exit(1);
        }
    }

    public void loadUserconfiguration(String userConfigFile) {
        loadUserconfiguration(new File(userConfigFile));
    }

    public void loadUserconfiguration(File userConfigFile) {
        //read user configuration file
        if (userConfigFile != null) {
            MessageHandler.logln("reading user configuration file");
            ConfigurationReader reader = new ConfigurationReader (
                                           InputHandler.fileInputSource(userConfigFile));
            if (errorDump) {
                reader.setDumpError(true);
            }
            try {
                reader.start();
            } catch (org.apache.fop.apps.FOPException error) {
                MessageHandler.errorln(
                  "Can't find user configuration file " +
                  userConfigFile);
                MessageHandler.errorln("using default values");
                if (errorDump) {
                    reader.dumpError(error);
                }
            }
        }
    }
}


