/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
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
