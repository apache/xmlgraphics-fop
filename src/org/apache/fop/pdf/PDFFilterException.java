/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

// Author:       Eric SCHAEFFER
// Description:  Filter Exception

package org.apache.fop.pdf;

public class PDFFilterException extends Exception {

    public PDFFilterException() {
        super();
    }

    public PDFFilterException(String message) {
        super(message);
    }

}
