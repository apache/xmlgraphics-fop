/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

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
import org.apache.fop.configuration.Configuration;

/**
 * super class for all classes which start Fop from the commandline
 *
 * Modified to use new streaming API by Mark Lillywhite, mark-fop@inomial.com
 */
public class CommandLineStarter extends Starter {

    CommandLineOptions commandLineOptions;
    boolean errorDump;

    public CommandLineStarter(CommandLineOptions commandLineOptions)
    throws FOPException {
        this.commandLineOptions = commandLineOptions;
        options.setCommandLineOptions(commandLineOptions);
        errorDump =
          Configuration.getBooleanValue("debugMode").booleanValue();
        super.setInputHandler(commandLineOptions.getInputHandler());
    }

    /**
     * Run the format.
     * @exception FOPException if there is an error during processing
     */
    public void run() throws FOPException {
        String version = Version.getVersion();

        log.info(version);

        XMLReader parser = inputHandler.getParser();
        setParserFeatures(parser);

        Driver driver = new Driver();
        driver.setLogger(log);
        driver.setBufferFile(commandLineOptions.getBufferFile());
        driver.initialize();

        if (errorDump) {
            driver.setErrorDump(true);
        }

        try {
            driver.setRenderer(commandLineOptions.getRenderer());
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(
                                      commandLineOptions.getOutputFile()));
            driver.setOutputStream(bos);
            driver.getRenderer().setOptions(
              commandLineOptions.getRendererOptions());
            driver.render(parser, inputHandler.getInputSource());
            bos.close();
            System.exit(0);
        } catch (Exception e) {
            if (e instanceof FOPException) {
                throw (FOPException) e;
            }
            throw new FOPException(e);
        }
    }

}

