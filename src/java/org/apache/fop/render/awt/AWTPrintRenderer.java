/*
 * $Id$
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
        super();
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

