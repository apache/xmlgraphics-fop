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

import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.util.Vector;

import org.apache.fop.apps.FOPException;
import org.apache.fop.render.java2d.Java2DRenderer;

/**
 * Renderer that prints through java.awt.PrintJob.
 * The actual printing is handled by Java2DRenderer
 * since both PrintRenderer and AWTRenderer need to
 * support printing.
 */
public class PrintRenderer extends Java2DRenderer implements Pageable {

    private static final int EVEN_AND_ALL = 0;

    private static final int EVEN = 1;

    private static final int ODD = 2;

    private int startNumber = 0;

    private int endNumber = -1;

    private int mode = EVEN_AND_ALL;

    private int copies = 1;

    private PrinterJob printerJob;

    /**
     * Creates a new PrintRenderer with the options set from system properties.
     */
    public PrintRenderer() {
        initializePrinterJob();
    }

    /**
     * Creates a new PrintRenderer and allows you to pass in a specific PrinterJob instance
     * that this renderer should work with.
     * @param printerJob the PrinterJob instance
     */
    public PrintRenderer(PrinterJob printerJob) {
        this.printerJob = printerJob;
        printerJob.setPageable(this);
    }
    
    private void initializePrinterJob() throws IllegalArgumentException {
        // read from command-line options
        copies = getIntProperty("copies", 1);
        startNumber = getIntProperty("start", 1) - 1;
        endNumber = getIntProperty("end", -1);
        String str = System.getProperty("even");
        if (str != null) {
            mode = Boolean.valueOf(str).booleanValue() ? EVEN : ODD;
        }

        printerJob = PrinterJob.getPrinterJob();
        printerJob.setJobName("FOP Document");
        printerJob.setCopies(copies);
        if (System.getProperty("dialog") != null) {
            if (!printerJob.printDialog()) {
                throw new IllegalArgumentException(
                        "Printing cancelled by operator");
            }
        }
        printerJob.setPageable(this);
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
    
    public void stopRenderer() throws IOException {
        super.stopRenderer();

        if (endNumber == -1) {
            // was not set on command line
            endNumber = getNumberOfPages();
        }

        Vector numbers = getInvalidPageNumbers();
        for (int i = numbers.size() - 1; i > -1; i--) {
            // removePage(Integer.parseInt((String)numbers.elementAt(i)));
        }

        try {
            printerJob.print();
        } catch (PrinterException e) {
            log.error(e);
            throw new IOException("Unable to print: " + e.getClass().getName()
                    + ": " + e.getMessage());
        }
        clearViewportList();
    }

    public static int getIntProperty(String name, int def) {
        String propValue = System.getProperty(name);
        if (propValue != null) {
            try {
                return Integer.parseInt(propValue);
            } catch (Exception e) {
                return def;
            }
        } else {
            return def;
        }
    }

    private Vector getInvalidPageNumbers() {
        Vector vec = new Vector();
        int max = getNumberOfPages();
        boolean isValid;
        for (int i = 0; i < max; i++) {
            isValid = true;
            if (i < startNumber || i > endNumber) {
                isValid = false;
            } else if (mode != EVEN_AND_ALL) {
                if (mode == EVEN && ((i + 1) % 2 != 0)) {
                    isValid = false;
                } else if (mode == ODD && ((i + 1) % 2 != 1)) {
                    isValid = false;
                }
            }

            if (!isValid) {
                vec.add(Integer.toString(i));
            }
        }
        return vec;
    }

    /** @see java.awt.print.Pageable#getPageFormat(int) */
    public PageFormat getPageFormat(int pageIndex)
            throws IndexOutOfBoundsException {
        try {
            if (pageIndex >= getNumberOfPages()) {
                return null;
            }
    
            PageFormat pageFormat = new PageFormat();
    
            Paper paper = new Paper();
    
            Rectangle2D dim = getPageViewport(pageIndex).getViewArea();
            double width = dim.getWidth();
            double height = dim.getHeight();
    
            // if the width is greater than the height assume lanscape mode
            // and swap the width and height values in the paper format
            if (width > height) {
                paper.setImageableArea(0, 0, height / 1000d, width / 1000d);
                paper.setSize(height / 1000d, width / 1000d);
                pageFormat.setOrientation(PageFormat.LANDSCAPE);
            } else {
                paper.setImageableArea(0, 0, width / 1000d, height / 1000d);
                paper.setSize(width / 1000d, height / 1000d);
                pageFormat.setOrientation(PageFormat.PORTRAIT);
            }
            pageFormat.setPaper(paper);
            return pageFormat;
        } catch (FOPException fopEx) {
            throw new IndexOutOfBoundsException(fopEx.getMessage());
        }
    }

    public Printable getPrintable(int pageIndex)
            throws IndexOutOfBoundsException {
        return this;
    }
}
