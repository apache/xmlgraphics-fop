package org.apache.fop.apps;

// SAX
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

// Java
import java.io.*;
import java.net.URL;


// FOP
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.configuration.Configuration;

/**
 *  super class for all classes which start Fop from the commandline
 */

public class CommandLineStarter extends Starter {
	
    CommandLineOptions commandLineOptions;
	boolean errorDump;
    	
    public CommandLineStarter (CommandLineOptions commandLineOptions) {
        this.commandLineOptions = commandLineOptions;
		options.setCommandLineOptions(commandLineOptions);
		errorDump = Configuration.getBooleanValue("debugMode").booleanValue();
		super.setInputHandler(commandLineOptions.getInputHandler());		
    }
	
    public void run() {
        String version = Version.getVersion();
        MessageHandler.logln(version);

        XMLReader parser = inputHandler.getParser();
        setParserFeatures(parser,errorDump);

		Driver driver = new Driver();
        if (errorDump) {
            driver.setErrorDump(true);
        }
			
        try {
            driver.setRenderer(commandLineOptions.getRenderer(), "");
            driver.addElementMapping("org.apache.fop.fo.StandardElementMapping");
            driver.addElementMapping("org.apache.fop.svg.SVGElementMapping");
            driver.addElementMapping("org.apache.fop.extensions.ExtensionElementMapping");
            driver.addPropertyList("org.apache.fop.fo.StandardPropertyListMapping");
            driver.addPropertyList("org.apache.fop.svg.SVGPropertyListMapping");
            driver.addPropertyList("org.apache.fop.extensions.ExtensionPropertyListMapping");
            driver.buildFOTree(parser,inputHandler.getInputSource());
            driver.format();
            driver.setOutputStream(new FileOutputStream(commandLineOptions.getOutputFile()));
            driver.render();
        } catch (Exception e) {
            MessageHandler.errorln("FATAL ERROR: " + e.getMessage());
            if (errorDump) {
                e.printStackTrace();
            }
            System.exit(0);
        }
    }
	
}

