/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.viewer;

/*
 * originally contributed by
 * Juergen Verwohlt: Juergen.Verwohlt@jCatalog.com,
 * Rainer Steinkuhle: Rainer.Steinkuhle@jCatalog.com,
 * Stanislav Gorkhover: Stanislav.Gorkhover@jCatalog.com
 */


import javax.swing.*;
import java.beans.PropertyChangeListener;

public class IconToolBar extends JToolBar {

    public JButton add(Action a) {
        String name = (String)a.getValue(Action.NAME);
        Icon icon = (Icon)a.getValue(Action.SMALL_ICON);
        return add(a, name, icon);
    }

    public JButton add(Action a, String name, Icon icon) {
        JButton b = new JButton(icon);
        b.setToolTipText(name);
        b.setEnabled(a.isEnabled());
        b.addActionListener(a);
        add(b);
        PropertyChangeListener actionPropertyChangeListener =
            createActionChangeListener(b);
        a.addPropertyChangeListener(actionPropertyChangeListener);
        return b;
    }

}


