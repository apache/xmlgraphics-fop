/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.viewer;

//Java
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import java.net.URL;

/**
 * This class represents UI-commands, which can be used as menu or toolbar
 * items<br>.
 * When the <code>Command</code> object receives action event, that object's
 * <code>doit</code> method is invoked. <code>doit</code> method by default
 * does nothing and the class customer have to override it to implement
 * any action handling logic.
 * Originally contributed by:
 * Juergen Verwohlt: Juergen.Verwohlt@jcatalog.com,
 * Rainer Steinkuhle: Rainer.Steinkuhle@jcatalog.com,
 * Stanislav Gorkhover: Stanislav.Gorkhover@jcatalog.com
 */
public class Command extends AbstractAction {

    private static String IMAGE_DIR = "Images/";

    /**
     * Creates <code>Command</code> object with a given name and
     * sets the name as a tooltip text. No associated icon image.
     */
    public Command(String name) {
        super(name);
        putValue(SHORT_DESCRIPTION, name);
    }

    /**
     * Creates <code>Command</code> object with a given name, the same
     * tooltip text and icon image if appropriate image file is found.
     */
    public Command(String name, String iconName) {
        super(name);
        putValue(SHORT_DESCRIPTION, name);
        URL url = getClass().getResource(IMAGE_DIR + iconName + ".gif");
        if (url != null) {
            putValue(SMALL_ICON, new ImageIcon(url));
        }
    }

    public void actionPerformed(ActionEvent e) {
        doit();
    }

    /**
     * Action handler, have to be overrided by subclasses.
     */
    public void doit() {
        //Do nothing
    }
}

