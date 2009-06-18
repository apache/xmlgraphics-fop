/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.viewer;
/*
 * Juergen Verwohlt: Juergen.Verwohlt@jcatalog.com,
 * Rainer Steinkuhle: Rainer.Steinkuhle@jcatalog.com,
 * Stanislav Gorkhover: Stanislav.Gorkhover@jcatalog.com
 */

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import java.net.*;


/**
 * Klasse für UI-Kommandos. Die Kommandos können in das Menüsystem oder
 * in eine Toolbar eingefügt werden.<br>
 * <code>Commands</code> unterstützen mehrsprachigkeit.<br>
 * Durch überschreiben der Methode <code>doit<code> kann die Klasse customisiert werden.
 * Über die Methode <code>undoit</code> kann Undo-Funktionalität unterstützt werden.<br>
 *
 * @author Juergen.Verwohlt@jcatalog.com
 * @version 1.0 18.03.99
 */
public class Command extends AbstractAction {

    public static String IMAGE_DIR = "/org/apache/fop/viewer/Images/";

    public Command(String name) {
        this(name, (ImageIcon)null);
    }

    public Command(String name, ImageIcon anIcon) {
        super(name, anIcon);
    }

    public Command(String name, String iconName) {
        super(name);
        String path = IMAGE_DIR + iconName + ".gif";
        URL url = getClass().getResource(path);
        if (url == null) {
            //log.error("Icon not found: " + path);
        } else
            putValue(SMALL_ICON, new ImageIcon(url));
    }

    public void actionPerformed(ActionEvent e) {
        doit();
    }

    public void doit() {
        //log.error("Not implemented.");
    }

    public void undoit() {
        //log.error("Not implemented.");
    }

}
