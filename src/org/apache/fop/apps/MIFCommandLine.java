/*-- $Id$ --

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

 */


//author : seshadrig



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
 * mainline class.
 *
 * Gets input and output filenames from the command line.
 * Creates a SAX Parser (defaulting to Xerces).
 *
 */
public class MIFCommandLine {

    private String foFile = null;
    private String mifFile = null;
    private String userConfigFile = null;
    private String baseDir = null;
    private boolean dumpConfiguration = false;

    /** show a full dump on error */
    private static boolean errorDump = false;

    public MIFCommandLine(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-d") || args[i].equals("--full-error-dump")) {
                errorDump = true;
            } else if (args[i].equals("-x")) {
                dumpConfiguration = true;
            } else if ((args[i].charAt(0) == '-') &&
                    (args[i].charAt(1) == 'c')) {
                userConfigFile = args[i].substring(2);
            } else if (args[i].charAt(0) == '-') {
                printUsage(args[i]);
            } else if (foFile == null) {
                foFile = args[i];
            } else if (mifFile == null) {
                mifFile = args[i];
            } else {
                printUsage(args[i]);
            }
        }
        if (foFile == null || mifFile == null) {
            printUsage(null);
        }
    }

    public void printUsage(String arg) {
        if (arg != null) {
            MessageHandler.errorln("Unknown argument: '"+arg + "'");
        } 
        MessageHandler.errorln("Usage: java [-d] [-x]" +
                               "[-cMyConfigFile] \n" +
                               "            org.apache.fop.apps.CommandLine " + "formatting-object-file pdf-file");
        MessageHandler.errorln("Options:\n" + "  -d or --full-error-dump      Show stack traces upon error");
        MessageHandler.errorln("  -x                           dump configuration information");
        MessageHandler.errorln("  -cMyConfigFile               use configuration file MyConfigFile");

        System.exit(1);
    }

    public void run() {
        Driver driver = new Driver();
        if (errorDump) {
            driver.setErrorDump(true);
        }
        if (userConfigFile != null) {
            driver.loadUserconfiguration(userConfigFile);
        }
        driver.setBaseDir(foFile);

        if (dumpConfiguration) {
            Configuration.dumpConfiguration();
            System.exit(0);
        }

        String version = Version.getVersion();
        MessageHandler.logln(version);

        XMLReader parser = createParser();

        if (parser == null) {
            MessageHandler.errorln("ERROR: Unable to create SAX parser");
            System.exit(1);
        }

        // setting the parser features
        try {
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes",
                              true);
        } catch (SAXException e) {
            MessageHandler.errorln("Error in setting up parser feature namespace-prefixes");
            MessageHandler.errorln("You need a parser which supports SAX version 2");
            if (errorDump) {
                e.printStackTrace();
            }
            System.exit(1);
        }

        try {
            driver.setRenderer("org.apache.fop.render.mif.MIFRenderer",
                               Version.getVersion());
            driver.addElementMapping("org.apache.fop.fo.StandardElementMapping");
            driver.addElementMapping("org.apache.fop.svg.SVGElementMapping");
            driver.addPropertyList("org.apache.fop.fo.StandardPropertyListMapping");
            driver.addPropertyList("org.apache.fop.svg.SVGPropertyListMapping");
            driver.buildFOTree(parser, fileInputSource(foFile));
            driver.format();
	    driver.setOutputStream(new FileOutputStream(mifFile));
            driver.render();
        } catch (Exception e) {
            MessageHandler.errorln("FATAL ERROR: " + e.getMessage());
            if (errorDump) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }


    /**
       * creates a SAX parser, using the value of org.xml.sax.parser
       * defaulting to org.apache.xerces.parsers.SAXParser
       *
       * @return the created SAX parser
       */
    static XMLReader createParser() {
        String parserClassName = System.getProperty("org.xml.sax.parser");
        if (parserClassName == null) {
            parserClassName = "org.apache.xerces.parsers.SAXParser";
        }
        org.apache.fop.messaging.MessageHandler.logln(
          "using SAX parser " + parserClassName);

        try {
            return (XMLReader) Class.forName(
                     parserClassName).newInstance();
        } catch (ClassNotFoundException e) {
            org.apache.fop.messaging.MessageHandler.errorln(
              "Could not find " + parserClassName);
            if (errorDump) {
                e.printStackTrace();
            }
        }
        catch (InstantiationException e) {
            org.apache.fop.messaging.MessageHandler.errorln(
              "Could not instantiate " + parserClassName);
            if (errorDump) {
                e.printStackTrace();
            }
        }
        catch (IllegalAccessException e) {
            org.apache.fop.messaging.MessageHandler.errorln(
              "Could not access " + parserClassName);
            if (errorDump) {
                e.printStackTrace();
            }
        }
        catch (ClassCastException e) {
            org.apache.fop.messaging.MessageHandler.errorln(
              parserClassName + " is not a SAX driver");
            if (errorDump) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
       * create an InputSource from a file name
       *
       * @param filename the name of the file
       * @return the InputSource created
       */
    public static InputSource fileInputSource(String filename) {

        /* this code adapted from James Clark's in XT */
        File file = new File(filename);
        String path = file.getAbsolutePath();
        String fSep = System.getProperty("file.separator");
        if (fSep != null && fSep.length() == 1)
            path = path.replace(fSep.charAt(0), '/');
        if (path.length() > 0 && path.charAt(0) != '/')
            path = '/' + path;
        try {
            return new InputSource(new URL("file", null, path).toString());
        } catch (java.net.MalformedURLException e) {
            throw new Error("unexpected MalformedURLException");
        }
    }

    /**
       * mainline method
       *
       * first command line argument is input file
       * second command line argument is output file
       *
       * @param command line arguments
       */
    public static void main(String[] args) {
        MIFCommandLine mifcmdLine = new MIFCommandLine(args);
        mifcmdLine.run();

    }

}
