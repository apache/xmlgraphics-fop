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
 
package org.apache.fop.render.awt;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;
import java.util.Vector;

public class AWTPrintRenderer extends AWTRenderer {

    private static final int EVEN_AND_ALL = 0;
    private static final int EVEN = 1;
    private static final int ODD = 2;
    
    private int startNumber;
    private int endNumber;
    private int mode = EVEN_AND_ALL;
    private int copies = 1;
    private PrinterJob printerJob;

    public AWTPrintRenderer() {
        initialize();
    }

    private void initialize() throws IllegalArgumentException {
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
                throw new IllegalArgumentException("Printing cancelled by operator");
            }
        }
        printerJob.setPageable(this);
    }   

    public void stopRenderer() throws IOException {
        super.stopRenderer();

        if (endNumber == -1) {
            endNumber = getNumberOfPages();
        }

        Vector numbers = getInvalidPageNumbers();
        for (int i = numbers.size() - 1; i > -1; i--) {
            // removePage(Integer.parseInt((String)numbers.elementAt(i)));
        }

        try {
            printerJob.print();
        } catch (PrinterException e) {
            e.printStackTrace();
            throw new IOException("Unable to print: " 
                + e.getClass().getName()
                + ": " + e.getMessage());
        }
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
                vec.add(i + "");
            }
        }
        return vec;
    }
} // class AWTPrintRenderer

