/*
 * $Id: Command.java,v 1.9 2003/03/07 10:09:58 jeremias Exp $
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

    private static final String IMAGE_DIR = "images/";

    /**
     * Creates <code>Command</code> object with a given name and
     * sets the name as a tooltip text. No associated icon image.
     * @param name of the command
     */
    public Command(String name) {
        super(name);
        putValue(SHORT_DESCRIPTION, name);
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
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
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

