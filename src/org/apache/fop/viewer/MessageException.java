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

import java.lang.Exception;

/**
 * Die Klasse <code>MessageException</code> ist eine Exception, die
 * mit einer Meldung und deren Parametern versehen werden kann.
 * Dadurch kann die Meldung über den Exception-Mechanismus an die
 * Aufrufer hochgereicht werden, bis schliesslich ein Aufrufer die
 * Meldung zur Anzeige bringt.
 *
 * @author Juergen.Verwohlt@jCatalog.com
 * @version 1.0 28.05.99
 *
 */
public class MessageException extends Exception {

    /**
     * Angabe der auslösenden Exception, wie z.B. NullPointerException.
     * Dieses Feld ist optional.
     */
    protected Exception exception;

    /**
     * ID der Meldung, die für diese Exception ausgegeben werden soll
     */
    protected String messageId;

    /**
     * Parameterliste zur Meldung
     */
    protected String[] parameterList;


    // Konstruktoren

    public MessageException() {
        this("UNKNOWN_EXCEPTION");
    }

    public MessageException(String aMessageId) {
        this(aMessageId, null);
    }

    public MessageException(String aMessageId, String[] aParameterList) {
        this(aMessageId, aParameterList, null);
    }

    public MessageException(String aMessageId, String[] aParameterList,
                            Exception anException) {
        super(aMessageId);
        messageId = aMessageId;
        parameterList = aParameterList;
        exception = anException;
    }

    // Zugriffsmethoden

    public String getMessageId() {
        return messageId;
    }

    public String[] getParameterList() {
        return parameterList;
    }

    public Exception getException() {
        return exception;
    }

}
