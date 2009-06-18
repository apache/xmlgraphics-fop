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
package org.apache.fop.viewer;
/*
 * Juergen Verwohlt: Juergen.Verwohlt@jcatalog.com,
 * Rainer Steinkuhle: Rainer.Steinkuhle@jcatalog.com,
 * Stanislav Gorkhover: Stanislav.Gorkhover@jcatalog.com
 */

import java.awt.event.ActionEvent;
import org.apache.fop.messaging.MessageHandler;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import java.net.URL;


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
            MessageHandler.errorln("Icon not found: " + path);
        } else
            putValue(SMALL_ICON, new ImageIcon(url));
    }

    public void actionPerformed(ActionEvent e) {
        doit();
    }

    public void doit() {
        MessageHandler.errorln("Not implemented.");
    }

    public void undoit() {
        MessageHandler.errorln("Not implemented.");
    }

}
