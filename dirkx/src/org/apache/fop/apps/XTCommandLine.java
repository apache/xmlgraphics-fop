package org.apache.xml.fop.apps;

import org.apache.xml.fop.render.pdf.PDFRenderer;
import org.apache.xml.fop.fo.StandardElementMapping;
import org.apache.xml.fop.svg.SVGElementMapping;

// James Clark
import com.jclark.xsl.sax.XSLProcessor;
import com.jclark.xsl.sax.XSLProcessorImpl;

// SAX
import org.xml.sax.Parser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

// Java
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.URL;

/**
 * mainline class for full transformation (via XT) + formatting/rendering.
 *
 * gets input, stylesheet and output filenames from the command line
 * creates an implementation of XSLProcessor, passing it the stylesheet
 * treats XSLProcessor as SAXParser
 *
 */
public class XTCommandLine extends CommandLine {

    /**
     * mainline method.
     *
     * first command line argument is XML input file
     * second command line argument is XSL stylesheet file
     * third command line argument is outputfile
     */
    public static void main(String[] args) {
	String version = Version.getVersion();
	System.err.println(version);
		
	if (args.length != 3) {
	    System.err.println("usage: java org.apache.xml.fop.apps.XTCommandLine xml-file xsl-stylesheet pdf-file");
	    System.exit(1);
	}
	
	Parser parser = createParser();
	
	if (parser == null) {
	    System.err.println("ERROR: Unable to create SAX parser");
	    System.exit(1);
	}
	
	XSLProcessor xslProcessor = new XSLProcessorImpl();
	xslProcessor.setParser(parser);
	
	try {
	    xslProcessor.loadStylesheet(fileInputSource(args[1]));

	    Driver driver = new Driver();
	    driver.setRenderer("org.apache.xml.fop.render.pdf.PDFRenderer",
			       version);
	    driver.addElementMapping("org.apache.xml.fop.fo.StandardElementMapping");
	    driver.addElementMapping("org.apache.xml.fop.svg.SVGElementMapping");
	    driver.setWriter(new PrintWriter(new FileWriter(args[2])));
	    driver.buildFOTree(parser, fileInputSource(args[0]));
	    driver.format();
	    driver.render();
	} catch (Exception e) {
	    System.err.println("FATAL ERROR: " + e.getMessage());
	    System.exit(1);
	}
    }
}
