/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package embedding;

//Java
import java.io.File;
import java.io.OutputStream;
import java.io.IOException;

//JAXP
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.Source;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.sax.SAXResult;

//Avalon
import org.apache.avalon.framework.ExceptionUtil;
import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;

//FOP
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;
import org.apache.fop.messaging.MessageHandler;

import embedding.model.*;

/**
 * This class demonstrates the conversion of an arbitrary object file to a 
 * PDF using JAXP (XSLT) and FOP (XSL:FO).
 */
public class ExampleObj2PDF {

    public void convertProjectTeam2PDF(ProjectTeam team, File xslt, File pdf) 
                throws IOException, FOPException, TransformerException {
                    
        //Construct driver
        Driver driver = new Driver();
        
        //Setup logger
        Logger logger = new ConsoleLogger(ConsoleLogger.LEVEL_INFO);
        driver.setLogger(logger);
        MessageHandler.setScreenLogger(logger);

        //Setup Renderer (output format)        
        driver.setRenderer(Driver.RENDER_PDF);

        //Setup output
        OutputStream out = new java.io.FileOutputStream(pdf);
        try {
            driver.setOutputStream(out);

            //Setup XSLT
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer(new StreamSource(xslt));
        
            //Setup input for XSLT transformation
            Source src = team.getSourceForProjectTeam();
        
            //Resulting SAX events (the generated FO) must be piped through to FOP
            Result res = new SAXResult(driver.getContentHandler());

            //Start XSLT transformation and FOP processing
            transformer.transform(src, res);
        } finally {
            out.close();
        }
    }


    public static void main(String[] args) {
        try {
            System.out.println("FOP ExampleObj2PDF\n");
            System.out.println("Preparing...");
            
            //Setup directories
            File baseDir = new File(".");
            File outDir = new File(baseDir, "out");
            outDir.mkdirs();

            //Setup input and output
            File xsltfile = new File(baseDir, "xml/xslt/projectteam2FO.xsl");
            File pdffile = new File(outDir, "ResultObj2PDF.pdf");

            System.out.println("Input: a ProjectTeam object");
            System.out.println("Stylesheet: " + xsltfile);
            System.out.println("Output: PDF (" + pdffile + ")");
            System.out.println();
            System.out.println("Transforming...");

            ExampleObj2PDF app = new ExampleObj2PDF();
            app.convertProjectTeam2PDF(ExampleObj2XML.createSampleProjectTeam(), xsltfile, pdffile);
            
            System.out.println("Success!");
        } catch (Exception e) {
            System.err.println(ExceptionUtil.printStackTrace(e));
            System.exit(-1);
        }
    }
}
