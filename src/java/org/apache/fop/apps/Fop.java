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
package org.apache.fop.apps;

// Java
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

// Avalon
import org.apache.avalon.framework.logger.ConsoleLogger;

// FOP
import org.apache.fop.render.awt.AWTRenderer;


/**
 * The main application class for the FOP command line interface (CLI).
 */
public class Fop {

    /**
     * The main routine for the command line interface
     * @param args the command line parameters
     */
    public static void main(String[] args) {
        CommandLineOptions options = null;
        InputHandler inputHandler = null;
        BufferedOutputStream bos = null;
        String version = Version.getVersion();

        try {
            Driver driver = new Driver();
            driver.enableLogging(new ConsoleLogger(ConsoleLogger.LEVEL_INFO));
            driver.getLogger().info(version);
            options = new CommandLineOptions(args);
            inputHandler = options.getInputHandler();

            try {
                if (options.getOutputMode() == CommandLineOptions.AWT_OUTPUT) {
                    driver.setRenderer(new AWTRenderer(inputHandler));
                } else {
                    driver.setRenderer(options.getRenderer());

                    if (options.getOutputFile() != null) {
                        bos = new BufferedOutputStream(new FileOutputStream(
                            options.getOutputFile()));
                        driver.setOutputStream(bos);
                    }
                }

                if (driver.getRenderer() != null) {
                    driver.getRenderer().setOptions(options.getRendererOptions());
                }
                driver.render(inputHandler);
            } finally {
                if (bos != null) {
                    bos.close();
                }
            }

            // System.exit(0) called to close AWT/SVG-created threads, if any.
            // AWTRenderer closes with window shutdown, so exit() should not
            // be called here
            if (options.getOutputMode() != CommandLineOptions.AWT_OUTPUT) {
                System.exit(0);
            }
        } catch (FOPException e) {
            if (e.getMessage() == null) {
                System.err.println("Exception occured with a null error message");
            } else {
                System.err.println("" + e.getMessage());
            }
            if (options != null && options.getLogger().isDebugEnabled()) {
                e.printStackTrace();
            } else {
                System.err.println("Turn on debugging for more information");
            }
            System.exit(1);
        } catch (java.io.IOException e) {
            System.err.println("" + e.getMessage());
            if (options != null && options.getLogger().isDebugEnabled()) {
                e.printStackTrace();
            } else {
                System.err.println("Turn on debugging for more information");
            }
            System.exit(1);
        }
    }
}

