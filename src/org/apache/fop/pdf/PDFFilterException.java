/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

/**
 * PDF Filter exception.
 * This is used for exceptions relating to use a PDF filter.
 *
 * @author Eric SCHAEFFER
 */
public class PDFFilterException extends Exception {
    /**
     * Create a basic filter exception.
     */
    public PDFFilterException() {
    }

    /**
     * Create a filter exception with a message.
     *
     * @param message the error message
     */
    public PDFFilterException(String message) {
        super(message);
    }
}

