/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.apps;

//FOP
import org.apache.fop.render.awt.AWTRenderer;
import org.apache.fop.viewer.PreviewDialog;
import org.apache.fop.viewer.Translator;

//Java
import javax.swing.UIManager;
import java.awt.*;
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
    private Driver driver;
    private XMLReader parser;

    public AWTStarter(CommandLineOptions commandLineOptions) throws FOPException {
        super(commandLineOptions);
        init();
    }

    private void init() throws FOPException {
        //Creates Translator according to the language
        String language = commandLineOptions.getLanguage();
        if (language == null)
            translator = new Translator(Locale.getDefault());
        else
            translator = new Translator(new Locale(language, ""));
        AWTRenderer renderer = new AWTRenderer(translator);
        frame = createPreviewDialog(renderer, translator);
        renderer.setComponent(frame);
        driver = new Driver();
        driver.setRenderer(renderer);
        parser = inputHandler.getParser();
        if (parser == null) {
            throw new FOPException("Unable to create SAX parser");
        }
    }

    /**
     * Runs formatting.
     */
    public void run() throws FOPException {
        driver.reset();
        try {
            frame.setStatus(translator.getString("Status.Build.FO.tree"));
            driver.render(parser, inputHandler.getInputSource());
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

