/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */

package org.apache.fop.apps;

import org.xml.sax.SAXException;


/**
 * Exception thrown when FOP has a problem
 */
public class FOPException extends Exception {

    private static final String EXCEPTION_SEPARATOR = "\n---------\n";

    private Throwable _exception;

    /**
     * create a new FOP Exception
     *
     * @param message descriptive message
     */
    public FOPException(String message) {
        super(message);
    }

    public FOPException(Throwable e) {
        super(e.getMessage());
        setException(e);
    }

    public FOPException(String message, Throwable e) {
        super(message);
        setException(e);
    }

    protected void setException(Throwable t) {
        _exception = t;
    }

    public Throwable getException() {
        return _exception;
    }

    protected Throwable getRootException() {
        Throwable result = _exception;

        if (result instanceof SAXException) {
            result = ((SAXException)result).getException();
        }
        if (result instanceof java.lang.reflect.InvocationTargetException) {
            result =
                ((java.lang.reflect.InvocationTargetException)result).getTargetException();
        }
        if (result != _exception) {
            return result;
        }
        return null;
    }


    public void printStackTrace() {
        synchronized (System.err) {
            super.printStackTrace();
            if (_exception != null) {
                System.err.println(EXCEPTION_SEPARATOR);
                _exception.printStackTrace();
            }
            if (getRootException() != null) {
                System.err.println(EXCEPTION_SEPARATOR);
                getRootException().printStackTrace();
            }
        }
    }

    public void printStackTrace(java.io.PrintStream stream) {
        synchronized (stream) {
            super.printStackTrace(stream);
            if (_exception != null) {
                stream.println(EXCEPTION_SEPARATOR);
                _exception.printStackTrace(stream);
            }
            if (getRootException() != null) {
                System.err.println(EXCEPTION_SEPARATOR);
                getRootException().printStackTrace(stream);
            }
        }
    }

    public void printStackTrace(java.io.PrintWriter writer) {
        synchronized (writer) {
            super.printStackTrace(writer);
            if (_exception != null) {
                writer.println(EXCEPTION_SEPARATOR);
                _exception.printStackTrace(writer);
            }
            if (getRootException() != null) {
                System.err.println(EXCEPTION_SEPARATOR);
                getRootException().printStackTrace(writer);
            }
        }
    }

}
