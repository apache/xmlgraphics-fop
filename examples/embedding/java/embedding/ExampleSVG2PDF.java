/*
 * $Id: ExampleFO2PDF.java,v 1.1.2.2 2003/02/25 16:06:34 jeremias Exp $
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
package embedding;

//Java
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//Avalon
import org.apache.avalon.framework.ExceptionUtil;

//Batik
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;

//FOP
import org.apache.fop.svg.PDFTranscoder;

/**
 * This class demonstrates the conversion of an SVG file to PDF using FOP.
 */
public class ExampleSVG2PDF {

    /**
     * Converts an FO file to a PDF file using FOP
     * @param svg the SVG file
     * @param pdf the target PDF file
     * @throws IOException In case of an I/O problem
     * @throws TranscoderException In case of a transcoding problem
     */
    public void convertSVG2PDF(File svg, File pdf) throws IOException, TranscoderException {
        
        //Create transcoder
        Transcoder transcoder = new PDFTranscoder();
        //Transcoder transcoder = new org.apache.fop.render.ps.PSTranscoder();
        
        //Setup input
        InputStream in = new java.io.FileInputStream(svg);
        try {
            TranscoderInput input = new TranscoderInput(in);
            
            //Setup output
            OutputStream out = new java.io.FileOutputStream(pdf);
            out = new java.io.BufferedOutputStream(out);
            try {
                TranscoderOutput output = new TranscoderOutput(out);
                
                //Do the transformation
                transcoder.transcode(input, output);
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }


    /**
     * Main method.
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try {
            System.out.println("FOP ExampleSVG2PDF\n");
            System.out.println("Preparing...");
            
            //Setup directories
            File baseDir = new File(".");
            File outDir = new File(baseDir, "out");
            outDir.mkdirs();

            //Setup input and output files            
            File svgfile = new File(baseDir, "xml/svg/helloworld.svg");
            File pdffile = new File(outDir, "ResultSVG2PDF.pdf");

            System.out.println("Input: SVG (" + svgfile + ")");
            System.out.println("Output: PDF (" + pdffile + ")");
            System.out.println();
            System.out.println("Transforming...");
            
            ExampleSVG2PDF app = new ExampleSVG2PDF();
            app.convertSVG2PDF(svgfile, pdffile);
            
            System.out.println("Success!");
        } catch (Exception e) {
            System.err.println(ExceptionUtil.printStackTrace(e));
            System.exit(-1);
        }
    }
}
