/*
 * Copyright 2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

// Derived from examples/embedding/java/embedding/ExampleXML2PDF.java
// in FOP-0.20.5

//Java
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.util.Vector;

//JAXP
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXResult;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;

// SAX
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;

// XML Commons
import org.apache.xml.resolver.tools.CatalogResolver;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;

//Avalon
import org.apache.avalon.framework.ExceptionUtil;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;

//FOP
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;
import org.apache.fop.messaging.MessageHandler;

/**
 * This class converts an XML file to PDF using 
 * JAXP (XSLT) and FOP (XSL:FO).
 */
public class BookMaker implements ErrorHandler {

    private Logger logger;

    private File xmlFile, xsltFile, outFile, pdfFile;
    private boolean useCatalog;
    private Vector xsltParams = null;

    public BookMaker() {
        //Setup logger
        logger = new ConsoleLogger(ConsoleLogger.LEVEL_INFO);
    }

    /**
     * org.xml.sax.ErrorHandler#warning
     **/
    public void warning(SAXParseException e) {
        logger.warn(e.toString());
    }

    /**
     * org.xml.sax.ErrorHandler#error
     **/
    public void error(SAXParseException e) {
        logger.error(e.toString());
    }

    /**
     * org.xml.sax.ErrorHandler#fatalError
     **/
    public void fatalError(SAXParseException e) throws SAXException {
        logger.error(e.toString());
        throw e;
    }

    public void makeBook() 
                throws IOException, FOPException, TransformerException,
                       FactoryConfigurationError,
                       ParserConfigurationException, SAXException {

        OutputStream out = null;

        try {

            Source xmlSource, xsltSource;
            Result result;
            CatalogResolver resolver = null;

            // Setup entity and URI resolver
            if (useCatalog) {
                resolver = new CatalogResolver();
                logger.info("Using " + resolver.getClass().getName()
                            + " as entity/URI resolver");
            }

            //Setup XSLT transformer
            TransformerFactory tFactory = TransformerFactory.newInstance();
            if (useCatalog) {
                tFactory.setURIResolver(resolver);
            }

            //Setup input and xslt sources
            if (useCatalog) {

                SAXParser parser;
                XMLReader xmlReader;
                FileInputStream fis;
                InputSource is;

                // throws FactoryConfigurationError
                SAXParserFactory sFactory = SAXParserFactory.newInstance();
                sFactory.setNamespaceAware(true);

                // Setup input source
                // throws ParserConfigurationException
                parser = sFactory.newSAXParser();
                // throws SAXException
                xmlReader = parser.getXMLReader();
                logger.info("Using " + xmlReader.getClass().getName()
                            + " as SAX parser");
                xmlReader.setErrorHandler(this);
                xmlReader.setEntityResolver(resolver);
        
                // Setup SAX source
                fis = new FileInputStream(xmlFile);
                is = new InputSource(fis);
                xmlSource = new SAXSource(xmlReader, is);

                // Setup xslt source
                // throws ParserConfigurationException
                parser = sFactory.newSAXParser();
                // throws SAXException
                xmlReader = parser.getXMLReader();
                logger.info("Using " + xmlReader.getClass().getName()
                            + " as SAX parser");
                xmlReader.setErrorHandler(this);
                xmlReader.setEntityResolver(resolver);
        
                // Setup SAX source
                fis = new FileInputStream(xsltFile);
                is = new InputSource(fis);
                xsltSource = new SAXSource(xmlReader, is);

            } else {
                xmlSource = new StreamSource(xmlFile);
                xsltSource = new StreamSource(xsltFile);
            }

            // Setup output result
            if (pdfFile != null) {
                //Setup FOP
                MessageHandler.setScreenLogger(logger);
                Driver driver = new Driver();
                driver.setLogger(logger);
                driver.setRenderer(Driver.RENDER_PDF);
                out = new FileOutputStream(pdfFile);
                driver.setOutputStream(out);
                //Resulting SAX events (the generated FO)
                // must be piped through to FOP
                result = new SAXResult(driver.getContentHandler());
            } else {
                out = new FileOutputStream(outFile);
                result = new StreamResult(out);
            }

            // Setup the transformer
            Transformer transformer
                = tFactory.newTransformer(xsltSource);
            logger.info("Using " + transformer.getClass().getName()
                        + " as TrAX transformer");

            // Set the value of parameters, if any, defined for stylesheet
            if (xsltParams != null) { 
                for (int i = 0; i < xsltParams.size(); i += 2) {
                    transformer.setParameter
                        ((String) xsltParams.elementAt(i),
                         (String) xsltParams.elementAt(i + 1));
                }
            }

            //Start XSLT transformation and FOP processing
            transformer.transform(xmlSource, result);

        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private static Options createOptions() {

        Options options = new Options();
        OptionGroup og;
        Option o;

        o = new Option("h", "help", false, "Print help");
        options.addOption(o);

        o = new Option("c", "useCatalog", false, "Use catalog");
        options.addOption(o);

        o = new Option("xml", "xmlFile", true, "XML input file");
        o.setArgName("file");
        options.addOption(o);

        o = new Option("xsl", "xslFile", true, "XSLT stylesheet");
        o.setArgName("file");
        options.addOption(o);

        // mutually exclusive output options
        og = new OptionGroup();
        o = new Option("out", "outFile", true, "(X)HTML/FO output file");
        o.setArgName("file");
        og.addOption(o);

        o = new Option("pdf", "pdfFile", true, "PDF output file");
        o.setArgName("file");
        og.addOption(o);

        options.addOptionGroup(og);

        o = new Option("p", "parameter", true,
                       "Parameter for the XSLT transformation");
        o.setArgs(2);
        o.setArgName("name value");
        options.addOption(o);

        return options;
    }

    public static void main(String[] args) {

        BookMaker app = new BookMaker();

        try {

            // Setup options
            Options options = createOptions();

            // Parse command line
            // GNU parser allow multi-letter short options
            Parser parser = new GnuParser();
            CommandLine cl = null;
            cl = parser.parse(options, args);
            if (cl.hasOption("h")) {
                // automatically generate the help statement
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("BookMaker", options);
                System.exit(0);
            }

            //Setup input and output files and parameters
            if (cl.hasOption("c")) {
                app.useCatalog = true;
            }
            if (cl.hasOption("xml")) {
                app.xmlFile = new File(cl.getOptionValue("xml"));
            }
            if (cl.hasOption("xsl")) {
                app.xsltFile = new File(cl.getOptionValue("xsl"));
            }   
            if (cl.hasOption("out")) {
                app.outFile = new File(cl.getOptionValue("out"));
            }
            if (cl.hasOption("pdf")) {
                app.pdfFile = new File(cl.getOptionValue("pdf"));
            }
            if (cl.hasOption("p")) {
                String[] params = cl.getOptionValues("p");
                app.xsltParams = new Vector();
                for (int i = 0; i < params.length; ++i) {
                    app.xsltParams.addElement(params[i]);
                }
            }

            app.logger.info("Input: XML (" + app.xmlFile + ")");
            app.logger.info("Stylesheet: " + app.xsltFile);
            if (app.pdfFile != null) {
                app.logger.info("Output: PDF (" + app.pdfFile + ")");
            } else {
                app.logger.info("Output: (X)HTML/FO (" + app.outFile + ")");
            }
            app.logger.info("");
            app.logger.info("Transforming...");
            
            app.makeBook();
            
            app.logger.info("Transforming done");
        } catch (Exception e) {
            app.logger.error(ExceptionUtil.printStackTrace(e));
            System.exit(1);
        }
    }
}
