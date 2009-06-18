/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.apps;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Exception thrown when FOP has a problem.
 */
public class FOPException extends SAXException {

    private static final String EXCEPTION_SEPARATOR = "\n---------\n";

    private String systemId;
    private int line;
    private int column;

    /**
     * create a new FOP Exception
     *
     * @param message descriptive message
     */
    public FOPException(String message) {
        super(message);
    }

    public FOPException(String message, String systemId, int line, int column) {
        super(message);
        this.systemId = systemId;
        this.line = line;
        this.column = column;
    }

    public FOPException(String message, Locator locator) {
        super(message);
        setLocator(locator);
    }


    /**
     *
     * @param e Throwable object
     */
    public FOPException(Exception e) {
        super(e);
    }

    /**
     *
     * @param message descriptive message
     * @param e Throwable object
     */
    public FOPException(String message, Throwable e) {
        super(message);
    }

    public void setLocator(Locator locator) {
        if (locator != null) {
            this.systemId = locator.getSystemId();
            this.line = locator.getLineNumber();
            this.column = locator.getColumnNumber();
        }
    }

    public void setLocation(String systemId, int line, int column) {
        this.systemId = systemId;
        this.line = line;
        this.column = column;
    }

    public boolean isLocationSet() {
        return line > 0;
    }

    public String getMessage() {
        if (isLocationSet()) {
            return systemId + ":" + line + "," + column + " " + super.getMessage();
        } else {
            return super.getMessage();
        }
    }

    /**
     * Attempts to recast the exception as other Throwable types.
     * @return the exception recast as another type if possible, otherwise null.
     */
    protected Throwable getRootException() {
        Throwable result = getException();

        if (result instanceof SAXException) {
            result = ((SAXException)result).getException();
        }
        if (result instanceof java.lang.reflect.InvocationTargetException) {
            result =
                ((java.lang.reflect.InvocationTargetException)result).getTargetException();
        }
        if (result != getException()) {
            return result;
        }
        return null;
    }

    /**
     * Write stack trace to stderr
     */
    public void printStackTrace() {
        synchronized (System.err) {
            super.printStackTrace();
            if (getException() != null) {
                System.err.println(EXCEPTION_SEPARATOR);
                getException().printStackTrace();
            }
            if (getRootException() != null) {
                System.err.println(EXCEPTION_SEPARATOR);
                getRootException().printStackTrace();
            }
        }
    }

    /**
     * write stack trace on a PrintStream
     * @param stream PrintStream on which to write stack trace
     */
    public void printStackTrace(java.io.PrintStream stream) {
        synchronized (stream) {
            super.printStackTrace(stream);
            if (getException() != null) {
                stream.println(EXCEPTION_SEPARATOR);
                getException().printStackTrace(stream);
            }
            if (getRootException() != null) {
                stream.println(EXCEPTION_SEPARATOR);
                getRootException().printStackTrace(stream);
            }
        }
    }

    /**
     * Write stack trace on a PrintWriter
     * @param writer PrintWriter on which to write stack trace
     */
    public void printStackTrace(java.io.PrintWriter writer) {
        synchronized (writer) {
            super.printStackTrace(writer);
            if (getException() != null) {
                writer.println(EXCEPTION_SEPARATOR);
                getException().printStackTrace(writer);
            }
            if (getRootException() != null) {
                writer.println(EXCEPTION_SEPARATOR);
                getRootException().printStackTrace(writer);
            }
        }
    }

}
