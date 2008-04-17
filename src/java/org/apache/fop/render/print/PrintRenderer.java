/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.fop.render.print;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.util.Map;

import org.apache.fop.apps.FOUserAgent;

/**
 * Renderer that prints through java.awt.PrintJob.
 * The actual printing is handled by Java2DRenderer
 * since both PrintRenderer and AWTRenderer need to
 * support printing.
 */
public class PrintRenderer extends PageableRenderer {

    /**
     * Printing parameter: the preconfigured PrinterJob to use,
     * datatype: java.awt.print.PrinterJob
     */
    public static final String PRINTER_JOB = "printerjob";
  
    /**
     * Printing parameter: the number of copies of the document to be printed,
     * datatype: a positive Integer
     */
    public static final String COPIES = "copies";
    
    
    private int copies = 1;

    private PrinterJob printerJob;

    /**
     * Creates a new PrintRenderer with the options set through the renderer options if a custom
     * PrinterJob is not given in FOUserAgent's renderer options.
     */
    public PrintRenderer() {
    }
    
    /**
     * Creates a new PrintRenderer and allows you to pass in a specific PrinterJob instance
     * that this renderer should work with.
     * @param printerJob the PrinterJob instance
     * @deprecated Please use the rendering options on the user agent to pass in the PrinterJob!
     */
    public PrintRenderer(PrinterJob printerJob) {
        this();
        this.printerJob = printerJob;
        printerJob.setPageable(this);
    }
    
    private void initializePrinterJob() {
        if (this.printerJob == null) {
            printerJob = PrinterJob.getPrinterJob();
            printerJob.setJobName("FOP Document");
            printerJob.setCopies(copies);
            if (System.getProperty("dialog") != null) {
                if (!printerJob.printDialog()) {
                    throw new RuntimeException(
                            "Printing cancelled by operator");
                }
            }
            printerJob.setPageable(this);
        }
    }

    /** {@inheritDoc} */
    public void setUserAgent(FOUserAgent agent) {
        super.setUserAgent(agent);
        
        Map rendererOptions = agent.getRendererOptions();
        
        Object printerJobO = rendererOptions.get(PrintRenderer.PRINTER_JOB);
        if (printerJobO != null) {
            if (!(printerJobO instanceof PrinterJob)) {
                throw new IllegalArgumentException(
                    "Renderer option " + PrintRenderer.PRINTER_JOB
                    + " must be an instance of java.awt.print.PrinterJob, but an instance of "
                    + printerJobO.getClass().getName() + " was given.");
            }
            printerJob = (PrinterJob)printerJobO;
            printerJob.setPageable(this);
        }
        Object o = rendererOptions.get(PrintRenderer.COPIES);
        if (o != null) {
            this.copies = getPositiveInteger(o);
        }
        initializePrinterJob();
    }

    /** @return the PrinterJob instance that this renderer prints to */
    public PrinterJob getPrinterJob() {
        return this.printerJob;
    }

    /** @return the ending page number */
    public int getEndNumber() {
        return endNumber;
    }
    
    /**
     * Sets the number of the last page to be printed.
     * @param end The ending page number
     */
    public void setEndPage(int end) {
        this.endNumber = end;
    }
    
    /** @return the starting page number */
    public int getStartPage() {
        return startNumber;
    }
    
    /**
     * Sets the number of the first page to be printed.
     * @param start The starting page number
     */
    public void setStartPage(int start) {
        this.startNumber = start;
    }
    
    /** {@inheritDoc} */
    public void stopRenderer() throws IOException {
        super.stopRenderer();

        try {
            printerJob.print();
        } catch (PrinterException e) {
            log.error(e);
            throw new IOException("Unable to print: " + e.getClass().getName()
                    + ": " + e.getMessage());
        }
        clearViewportList();
    }

}
