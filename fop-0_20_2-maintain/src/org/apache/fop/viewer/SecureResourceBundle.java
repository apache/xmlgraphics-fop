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

import java.util.ResourceBundle;
import java.util.Enumeration;
import org.apache.fop.messaging.MessageHandler;
import java.io.InputStream;


/**
 * Die Klasse <code>SecureResourceBundle</code> ist ein Resourceundle, das im Falle eines fehlenden
 * Eintrages keinen Absturz verursacht, sondern die Meldung
 * <strong>Key <i>key</i> not found</strong> zurückgibt.
 *
 * @author Stanislav.Gorkhover@jCatalog.com
 * @version 1.0 18.03.1999
 */
public class SecureResourceBundle extends ResourceBundle
    implements Translator {

    // Fehlende keys mit einer Meldung zurückgeben.
    private boolean isMissingEmphasized = false;

    // private Properties lookup = new Properties();
    private LoadableProperties lookup = new LoadableProperties();

    private boolean isSourceFound = true;

    public void setMissingEmphasized(boolean flag) {
        isMissingEmphasized = flag;
    }

    /**
     * Kreiert ein ResourceBundle mit der Quelle in <strong>in</strong>.
     */

    public SecureResourceBundle(InputStream in) {
        try {
            lookup.load(in);
        } catch (Exception ex) {
            MessageHandler.logln("Exception catched: " + ex.getMessage());
            isSourceFound = false;
        }
    }



    public Enumeration getKeys() {
        return lookup.keys();
    }



    /**
     * Händelt den abgefragten Key, liefert entweder den zugehörigen Wert oder eine Meldung.
     * Die <strong>null</strong> wird nie zurückgegeben.
     * Schreibt die fehlenden Suchschlüssel in die Protokoll-Datei.
     * @return <code>Object</code><UL>
     * <LI>den zu dem Suchschlüssel <strong>key</strong> gefundenen Wert, falls vorhanden, <br>
     * <LI>Meldung <strong>Key <i>key</i> not found</strong>, falls der Suchschlüssel fehlt
     * und die Eigenschaft "jCatalog.DevelopmentStartModus" in der ini-Datei aus true gesetzt ist.
     * <LI>Meldung <strong>Key is null</strong>, falls der Suchschlüssel <code>null</code> ist.
     * </UL>
     *
     */
    public Object handleGetObject(String key) {

        if (key == null)
            return "Key is null";

        Object obj = lookup.get(key);
        if (obj != null)
            return obj;
        else {
            if (isMissingEmphasized) {
                MessageHandler.logln(getClass().getName() + ": missing key: "
                                     + key);
                return getMissedRepresentation(key.toString());
            } else
                return key.toString();
        }
    }

    /**
     * Stellt fest, ob es den Key gibt.
     */
    public boolean contains(String key) {
        return (key == null || lookup.get(key) == null) ? false : true;
    }


    private String getMissedRepresentation(String str) {
        return "<!" + str + "!>";
    }

    public boolean isSourceFound() {
        return isSourceFound;
    }

}
