/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
 
package embedding;

// Java
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// SAX
import org.xml.sax.InputSource;

// FOP
import org.apache.fop.apps.Driver;
import org.apache.fop.apps.FOPException;

/**
 * This class demonstrates the conversion of an FO file to PDF using FOP.
 */
public class ExampleFO2PDF {

    /**
     * Converts an FO file to a PDF file using FOP
     * @param fo the FO file
     * @param pdf the target PDF file
     * @throws IOException In case of an I/O problem
     * @throws FOPException In case of a FOP problem
     */
    public void convertFO2PDF(File fo, File pdf) throws IOException, FOPException {
        
        // Construct driver
        Driver driver = new Driver();
        
        // Setup Renderer (output format)        
        driver.setRenderer(Driver.RENDER_PDF);
        
        // Setup output
        OutputStream out = new java.io.FileOutputStream(pdf);
        out = new java.io.BufferedOutputStream(out);
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


    /**
     * Main method.
     * @param args command-line arguments
     */
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
            e.printStackTrace(System.err);
            System.exit(-1);
        }
    }
}
