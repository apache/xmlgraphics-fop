/*
 * $Id: AWTStarter.java,v 1.18 2003/02/27 10:13:05 jeremias Exp $
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

//FOP
import org.apache.fop.render.awt.AWTRenderer;
import org.apache.fop.viewer.PreviewDialog;
import org.apache.fop.viewer.Translator;

//Java
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;

// SAX
import org.xml.sax.XMLReader;

/**
 * AWT Viewer starter.
 * Originally contributed by:
 * Juergen Verwohlt: Juergen.Verwohlt@jCatalog.com,
 * Rainer Steinkuhle: Rainer.Steinkuhle@jCatalog.com,
 * Stanislav Gorkhover: Stanislav.Gorkhover@jCatalog.com
 * Modified to use streaming API by Mark Lillywhite, mark-fop@inomial.com
 */
public class AWTStarter extends CommandLineStarter {
    private PreviewDialog frame;
    private Translator translator;
    private Session session;
    private XMLReader parser;

    /**
     * Construct an AWTStarter
     * @param commandLineOptions the parsed command line options
     * @throws FOPException if anything goes wrong during initialization.
     */
    public AWTStarter(CommandLineOptions commandLineOptions)
        throws FOPException {
        super(commandLineOptions);
        init();
    }

    private void init() throws FOPException {
        //Creates Translator according to the language
        String language = commandLineOptions.getLanguage();
        if (language == null) {
            translator = new Translator(Locale.getDefault());
        } else {
            translator = new Translator(new Locale(language, ""));
        }
        AWTRenderer renderer = new AWTRenderer(translator);
        frame = createPreviewDialog(renderer, translator);
        renderer.setComponent(frame);
        session = new Session();
        session.setRenderer(renderer);
        parser = inputHandler.getParser();
        if (parser == null) {
            throw new FOPException("Unable to create SAX parser");
        }
        setParserFeatures(parser);
    }

    /**
     * Runs formatting.
     * @throws FOPException FIXME should not happen.
     */
    public void run() throws FOPException {
        session.reset();
        try {
            frame.setStatus(translator.getString("Status.Build.FO.tree"));
            session.render(parser, inputHandler.getInputSource());
            frame.setStatus(translator.getString("Status.Show"));
            frame.showPage();
        } catch (Exception e) {
            frame.reportException(e);
        }
    }

    private PreviewDialog createPreviewDialog(AWTRenderer renderer,
            Translator res) {
        PreviewDialog frame = new PreviewDialog(this, renderer, res);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent we) {
                System.exit(0);
            }
        });

        //Centers the window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2,
                          (screenSize.height - frameSize.height) / 2);
        frame.setVisible(true);
        return frame;
    }
}

