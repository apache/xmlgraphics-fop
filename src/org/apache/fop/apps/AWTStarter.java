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
/*
 * originally contributed by
 * Juergen Verwohlt: Juergen.Verwohlt@jCatalog.com,
 * Rainer Steinkuhle: Rainer.Steinkuhle@jCatalog.com,
 * Stanislav Gorkhover: Stanislav.Gorkhover@jCatalog.com
 * Modified to use streaming API by Mark Lillywhite, mark-fop@inomial.com
 */
import org.apache.fop.viewer.PreviewDialog;
import org.apache.fop.viewer.Translator;
import org.apache.fop.viewer.SecureResourceBundle;
import org.apache.fop.viewer.UserMessage;
import org.apache.fop.render.awt.AWTRenderer;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


import javax.swing.UIManager;
import java.awt.Dimension;
import java.awt.Toolkit;

// SAX
import org.xml.sax.XMLReader;

// Avalon
import org.apache.avalon.framework.logger.ConsoleLogger;

// Java
import java.io.InputStream;
import java.net.URL;

/**
 * initialize AWT previewer
 */
public class AWTStarter extends CommandLineStarter {

    PreviewDialog frame;
    AWTRenderer renderer;
    protected Driver driver;
    protected XMLReader parser;
    public static final String TRANSLATION_PATH =
        "/org/apache/fop/viewer/resources/";

    private Translator resource;

    public AWTStarter(CommandLineOptions commandLineOptions)
    throws FOPException {
        super(commandLineOptions);
        init();
    }

    private void init() throws FOPException {
        try {
            UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String language = commandLineOptions.getLanguage();

        if (language == null) {
            try {
                language = System.getProperty("user.language");
            } catch(SecurityException se) {
                // if this is running in a secure place
            }
        }
        resource = getResourceBundle(TRANSLATION_PATH + "resources."
                                     + language);

        UserMessage.setTranslator(getResourceBundle(TRANSLATION_PATH
                                  + "messages."
                                  + language));

        resource.setMissingEmphasized(false);
        renderer = new AWTRenderer(resource);
        frame = createPreviewDialog(renderer, resource);
        renderer.setProgressListener(frame);
        renderer.setComponent(frame);
        driver = new Driver();
        driver.setLogger(new ConsoleLogger(ConsoleLogger.LEVEL_INFO));
        if (errorDump) {
            driver.setErrorDump(true);
        }
        driver.setRenderer(renderer);
        // init parser
        frame.progress(resource.getString("Init parser") + " ...");
        parser = inputHandler.getParser();
        if (parser == null) {
            throw new FOPException("Unable to create SAX parser");
        }
    }


    public void run() throws FOPException {
        driver.reset();
        try {
            // build FO tree: time
            frame.progress(resource.getString("Build FO tree") + " ...");
            driver.render(parser, inputHandler.getInputSource());

            frame.progress(resource.getString("Show"));
            frame.showPage();

        } catch (Exception e) {
            frame.reportException(e);
            if (e instanceof FOPException) {
                throw (FOPException)e;
            }
            throw new FOPException(e);
        }

    }

    protected PreviewDialog createPreviewDialog(AWTRenderer renderer,
            Translator res) {
        PreviewDialog frame = new PreviewDialog(this, renderer, res);
        frame.validate();
        frame.addWindowListener(new WindowAdapter() {
                public void windowClosed(WindowEvent we) {
                    System.exit(0);
                }
            });

        // center window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height)
            frameSize.height = screenSize.height;
        if (frameSize.width > screenSize.width)
            frameSize.width = screenSize.width;
        frame.setLocation((screenSize.width - frameSize.width) / 2,
                          (screenSize.height - frameSize.height) / 2);
        frame.setVisible(true);
        return frame;
    }



    private SecureResourceBundle getResourceBundle(String path) {
        InputStream in = null;

        try {
            URL url = getClass().getResource(path);

            /* The following code was added by Alex Alishevskikh [alex@openmechanics.net]
               to fix for crashes on machines with unsupported user languages */
        if (url == null) {
                // if the given resource file not found, the english resource uses as default
                path = path.substring(0, path.lastIndexOf(".")) + ".en";
                url = getClass().getResource(path);
        }

            in = url.openStream();
        } catch (Exception ex) {
            log.error("Can't find URL to: <" + path + "> "
                                 + ex.getMessage(), ex);
        }
        return new SecureResourceBundle(in);
    }

}



