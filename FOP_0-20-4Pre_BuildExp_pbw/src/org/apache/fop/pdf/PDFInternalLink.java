/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

/**
 * class used to create a PDF internal link
 */
public class PDFInternalLink extends PDFAction {


    String goToReference;

    /**
     * create an internal link instance.
     *
     * @param goToReference the GoTo Reference to which the link should point
     */
    public PDFInternalLink(String goToReference) {

        this.goToReference = goToReference;
    }

    /**
     * returns the action ncecessary for an internal link
     *
     * @return the action to place next to /A within a Link
     */
    public String getAction() {
        return goToReference;
    }

    /**
     * there is nothing to return for the toPDF method, as it should not be called
     *
     * @return an empty string
     */
    public byte[] toPDF() {
        return new byte[0];
    }

}
