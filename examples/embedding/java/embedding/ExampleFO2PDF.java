/*
 * $Id$
 * Copyright (C) 2002-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package embedding;

//Java
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//SAX
import org.xml.sax.InputSource;

//Avalon
import org.apache.avalon.framework.ExceptionUtil;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.ConsoleLogger;

//FOP
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;
import org.apache.fop.messaging.MessageHandler;

/**
 * This class demonstrates the conversion of an FO file to PDF using FOP.
 */
public class ExampleFO2PDF {

    public void convertFO2PDF(File fo, File pdf) throws IOException, FOPException {
        
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

            //Setup input
            InputStream in = new java.io.FileInputStream(fo);
            try {
                driver.setInputSource(new InputSource(in));
            
                //Process FO
                driver.run();
            } finally {
                in.close();
            }
        } finally {
            out.close();
        }
    }


    public static void main(String[] args) {
        try {
            System.out.println("FOP ExampleFO2PDF\n");
            System.out.println("Preparing...");
            
            //Setup directories
            File baseDir = new File(".");
            File outDir = new File(baseDir, "out");
            outDir.mkdirs();

            //Setup input and output files            
            File fofile = new File(baseDir, "xml/fo/helloworld.fo");
            File pdffile = new File(outDir, "ResultFO2PDF.pdf");

            System.out.println("Input: XSL-FO (" + fofile + ")");
            System.out.println("Output: PDF (" + pdffile + ")");
            System.out.println();
            System.out.println("Transforming...");
            
            ExampleFO2PDF app = new ExampleFO2PDF();
            app.convertFO2PDF(fofile, pdffile);
            
            System.out.println("Success!");
        } catch (Exception e) {
            System.err.println(ExceptionUtil.printStackTrace(e));
            System.exit(-1);
        }
    }
}
