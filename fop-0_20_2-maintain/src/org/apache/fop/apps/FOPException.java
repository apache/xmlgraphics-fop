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
package org.apache.fop.apps;

import org.xml.sax.SAXException;


/**
 * Exception thrown when FOP has a problem
 */
public class FOPException extends Exception {

    private static final String EXCEPTION_SEPARATOR = "\n---------\n";

    private Throwable _exception;
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
        systemId = null;
        line = -1;
        column = -1;
    }

    public FOPException(String message, String systemId, int line, int column) {
        super(message);
        this.systemId = systemId;
        this.line = line;
        this.column = column;
    }

    public FOPException(Throwable e) {
        super(e.getMessage());
        setException(e);
        systemId = null;
        line = -1;
        column = -1;
    }

    public FOPException(Throwable e, String systemId, int line, int column) {
        super(e.getMessage());
        setException(e);
        this.systemId = systemId;
        this.line = line;
        this.column = column;
    }

    public FOPException(String message, Throwable e) {
        super(message);
        setException(e);
        systemId = null;
        line = -1;
        column = -1;
    }

    public FOPException(String message, Throwable e, String systemId, int line, int column) {
        super(message);
        setException(e);
        this.systemId = systemId;
        this.line = line;
        this.column = column;
    }

    protected void setException(Throwable t) {
        _exception = t;
    }

    public Throwable getException() {
        return _exception;
    }

    public void setLocation(String systemId, int line, int column) {
        this.systemId = systemId;
        this.line = line;
        this.column = column;
    }

    public boolean isLocationSet() {
        return line>=0;
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

    public String getMessage() {
        if (line>=0) {
            return systemId+':'+line+':'+column+' '+super.getMessage();
        } else {
            return super.getMessage();
        }
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
                stream.println(EXCEPTION_SEPARATOR);
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
                writer.println(EXCEPTION_SEPARATOR);
                getRootException().printStackTrace(writer);
            }
        }
    }

}
