/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;

/*
 * originally contributed by
 * Stanislav Gorkhover: stanislav.gorkhover@jcatalog.com
 * jCatalog Software AG
 * 
 * Updated by Mark Lillywhite, mark-fop@inomial.com. Modified to
 * handle the print job better, added -Ddialog option, removed 
 * (apparently) redundant copies code, generally cleaned up, and
 * added interfaces to the new Render API.
 */


import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.awt.Graphics;
import java.awt.print.*;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Vector;

import org.apache.fop.render.awt.AWTRenderer;
import org.apache.fop.layout.AreaTree;
import org.apache.fop.layout.Page;

/**
 * This class prints a xsl-fo dokument without interaction.
 * At the moment java has not the possibility to configure the printer and it's
 * options without interaction (30.03.2000).
 * This class allows to print a set of pages (from-to), even/odd pages and many copies.
 * - Print from page xxx: property name - start, value int
 * - Print to page xxx: property name - end, value int
 * - Print even/odd pages: property name - even, value boolean
 * - Print xxx copies: property name - copies, value int
 *
 */
public class PrintStarter extends CommandLineStarter {

    public PrintStarter(CommandLineOptions options) throws FOPException {
        super(options);
    }

    public void run() throws FOPException {
        Driver driver = new Driver();
        if (errorDump) {
            driver.setErrorDump(true);
        }

        String version = Version.getVersion();
        //log.debug(version);

        XMLReader parser = inputHandler.getParser();

        setParserFeatures(parser);

        PrinterJob pj = PrinterJob.getPrinterJob();
        if(System.getProperty("dialog") != null)
            if(!pj.printDialog())
                throw new FOPException("Printing cancelled by operator");

        PrintRenderer renderer = new PrintRenderer(pj);
        int copies = getIntProperty("copies", 1);
        pj.setCopies(copies);

        //renderer.setCopies(copies);

        try {
            driver.setRenderer(renderer);
            driver.render(parser, inputHandler.getInputSource());
        } catch (Exception e) {
            if (e instanceof FOPException) {
                throw (FOPException)e;
            }
            throw new FOPException(e);
        }

        System.exit(0);
    }
    int getIntProperty(String name, int def) {
        String propValue = System.getProperty(name);
        if(propValue != null) {
            try {
                return Integer.parseInt(propValue);
            } catch (Exception e) {
                return def;
            }
        } else {
            return def;
        }
    }

    class PrintRenderer extends AWTRenderer {

        private static final int EVEN_AND_ALL = 0;
        private static final int EVEN = 1;
        private static final int ODD = 2;

        private int startNumber;
        private int endNumber;
        private int mode = EVEN_AND_ALL;
        private int copies = 1;
        private PrinterJob printerJob;

        PrintRenderer(PrinterJob printerJob) {
            super(null);

            this.printerJob = printerJob;
            startNumber = getIntProperty("start", 1) - 1;
            endNumber = getIntProperty("end", -1);

            printerJob.setPageable(this);

            mode = EVEN_AND_ALL;
            String str = System.getProperty("even");
            if (str != null) {
                try {
                    mode = Boolean.valueOf(str).booleanValue() ? EVEN : ODD;
                } catch (Exception e) {}

            }

        }

        public void stopRenderer(OutputStream outputStream)
        throws IOException {
            super.stopRenderer();

            if(endNumber == -1)
                endNumber = getPageCount();

            Vector numbers = getInvalidPageNumbers();
            for (int i = numbers.size() - 1; i > -1; i--) {
                //removePage(Integer.parseInt((String)numbers.elementAt(i)));
            }

            try {
                printerJob.print();
            } catch (PrinterException e) {
                e.printStackTrace();
                throw new IOException(
                    "Unable to print: " + e.getClass().getName() +
                    ": " + e.getMessage());
            }
        }

        public void renderPage(Page page) {
            pageWidth = (int)((float)page.getWidth() / 1000f);
            pageHeight = (int)((float)page.getHeight() / 1000f);
        }

        private Vector getInvalidPageNumbers() {

            Vector vec = new Vector();
            int max = getPageCount();
            boolean isValid;
            for (int i = 0; i < max; i++) {
                isValid = true;
                if (i < startNumber || i > endNumber) {
                    isValid = false;
                } else if (mode != EVEN_AND_ALL) {
                    if (mode == EVEN && ((i + 1) % 2 != 0))
                        isValid = false;
                    else if (mode == ODD && ((i + 1) % 2 != 1))
                        isValid = false;
                }

                if (!isValid)
                    vec.add(i + "");
            }

            return vec;
        }

        /* TODO: I'm totally not sure that this is necessary -Mark
        void setCopies(int val) {
            copies = val;
            Vector copie = tree.getPages();
            for (int i = 1; i < copies; i++) {
                tree.getPages().addAll(copie);
            }

    }
        */
    }    // class PrintRenderer
}        // class PrintCommandLine

