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

import org.apache.fop.messaging.MessageHandler;

import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Hashtable;

/**
 * Erweitert Hashtable um die Methode load.
 * Die Zeilen der Textdatei, die mit # oder ! anfangen sind Kommentarzeilen.
 * Eine gültige Zeile ist entweder eine Kommentarzeile oder eine Zeile mit dem
 * Gleichheitszeichen "in der Mitte".
 * Die Klasse LoadableProperties lässt im Gegensatz zu der Klasse Properties die
 * Schlüsselwerte mit Leerzeichen zu.
 *
 * @version 02.12.99
 * @author Stanislav.Gorkhover@jCatalog.com
 *
 */
public class LoadableProperties extends Hashtable {

    public LoadableProperties() {
        super();
    }


    public void load(InputStream inStream) throws IOException {

        BufferedReader in = new BufferedReader(new InputStreamReader(inStream,
                "UTF-8"));

        String aKey;
        String aValue;
        int index;
        String line = getNextLine(in);
        while (line != null) {
            line = line.trim();
            if (isValid(line)) {
                index = line.indexOf("=");
                aKey = line.substring(0, index);
                aValue = line.substring(index + 1);
                put(aKey, aValue);
            }
            line = getNextLine(in);
        }
    }


    private boolean isValid(String str) {
        if (str == null)
            return false;
        if (str.length() > 0) {
            if (str.startsWith("#") || str.startsWith("!")) {
                return false;
            }
        } else {
            return false;
        }

        int index = str.indexOf("=");
        if (index > 0 && str.length() > index) {
            return true;
        } else {
            MessageHandler.logln(getClass().getName()
                                 + ": load(): invalid line " + str + "."
                                 + " Character '=' missed.");
            return false;
        }
    }

    private String getNextLine(BufferedReader br) {
        try {
            return br.readLine();
        } catch (Exception e) {
            return null;
        }

    }


}
