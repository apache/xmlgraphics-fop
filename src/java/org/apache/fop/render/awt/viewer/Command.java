/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.render.awt.viewer;

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

    private static final String IMAGE_DIR = "images/";

    /**
     * Creates <code>Command</code> object with a given name and
     * sets the name as a tooltip text. No associated icon image.
     * @param name of the command
     * @param mnemonic A Key
     */
    public Command(String name, int mnemonic) {
        super(name);
        putValue(SHORT_DESCRIPTION, name);
        if (mnemonic > 0) {
            putValue(MNEMONIC_KEY, new Integer(mnemonic));
        }
    }

    /**
     * Creates <code>Command</code> object with a given name, the same
     * tooltip text and icon image if appropriate image file is found.
     * @param name name of the command
     * @param iconName name of the icon
     */
    public Command(String name, String iconName) {
        super(name);
        putValue(SHORT_DESCRIPTION, name);
        URL url = getClass().getResource(IMAGE_DIR + iconName + ".gif");
        if (url != null) {
            putValue(SMALL_ICON, new ImageIcon(url));
        }
    }

    /**
     * {@inheritDoc}
     */
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

