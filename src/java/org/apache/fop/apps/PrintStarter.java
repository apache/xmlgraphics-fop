/*
 * $Id: PrintStarter.java,v 1.14 2003/02/27 10:13:06 jeremias Exp $
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
import java.awt.print.PrinterJob;
import org.apache.fop.render.awt.AWTPrintRenderer;

/**
 * This class prints a XSL-FO dokument without interaction.
 * At the moment java has not the possibility to configure the printer and it's
 * options without interaction (30.03.2000).
 * This class allows to print a set of pages (from-to), even/odd pages and many copies.
 * - Print from page xxx: property name - start, value int
 * - Print to page xxx: property name - end, value int
 * - Print even/odd pages: property name - even, value boolean
 * - Print xxx copies: property name - copies, value int
 */
public class PrintStarter extends CommandLineStarter {

    /**
     * @see org.apache.fop.apps.CommandLineStarter#CommandLineStarter(CommandLineOptions)
     */
    public PrintStarter(CommandLineOptions options) throws FOPException {
        super(options);
    }

    /**
     * @see org.apache.fop.apps.Starter#run()
     */
    public void run() throws FOPException {
        Driver driver = new Driver();

        PrinterJob pj = PrinterJob.getPrinterJob();
        if (System.getProperty("dialog") != null) {
            if (!pj.printDialog()) {
                throw new FOPException("Printing cancelled by operator");
            }
        }

        AWTPrintRenderer renderer = new AWTPrintRenderer(pj);
        int copies = getIntProperty("copies", 1);
        pj.setCopies(copies);

        //renderer.setCopies(copies);

        try {
            driver.setRenderer(renderer);
            driver.render(inputHandler);
        } catch (Exception e) {
            if (e instanceof FOPException) {
                throw (FOPException)e;
            }
            throw new FOPException(e);
        }

        System.exit(0);
    }

    private int getIntProperty(String name, int def) {
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
} // class PrintStarter

