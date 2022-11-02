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

package org.apache.fop.render.afp.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Handy class for wrapping runtime Exceptions with a root cause.
 * This technique is no longer necessary in Java 1.4, which provides
 * built-in support for exception nesting. Thus exceptions in applications
 * written to use Java 1.4 need not extend this class.
 *
 */
public abstract class NestedRuntimeException extends RuntimeException {

    /** Root cause of this nested exception */
    private Throwable underlyingException;

    /**
     * Construct a <code>NestedRuntimeException</code> with the specified detail message.
     * @param msg The detail message.
     */
    public NestedRuntimeException(String msg) {
        super(msg);
    }

    /**
     * Construct a <code>NestedRuntimeException</code> with the specified
     * detail message and nested exception.
     * @param msg The detail message.
     * @param t The nested exception.
     */
    public NestedRuntimeException(String msg, Throwable t) {
        super(msg);
        underlyingException = t;

    }

    /**
     * Gets the original triggering exception
     * @return The original exception as a throwable.
     */
    public Throwable getUnderlyingException() {

        return underlyingException;

    }

    /**
     * Return the detail message, including the message from the nested
     * exception if there is one.
     * @return The detail message.
     */
    public String getMessage() {

        if (underlyingException == null) {
            return super.getMessage();
        } else {
            return super.getMessage()
            + "; nested exception is "
                + underlyingException.getClass().getName();
        }

    }

    /**
     * Print the composite message and the embedded stack trace to the specified stream.
     * @param ps the print stream
     */
    public void printStackTrace(PrintStream ps) {
        if (underlyingException == null) {
            super.printStackTrace(ps);
        } else {
            ps.println(this);
            underlyingException.printStackTrace(ps);
        }
    }

    /**
     * Print the composite message and the embedded stack trace to the specified writer.
     * @param pw the print writer
     */
    public void printStackTrace(PrintWriter pw) {
        if (underlyingException == null) {
            super.printStackTrace(pw);
        } else {
            pw.println(this);
            underlyingException.printStackTrace(pw);
        }
    }

}
