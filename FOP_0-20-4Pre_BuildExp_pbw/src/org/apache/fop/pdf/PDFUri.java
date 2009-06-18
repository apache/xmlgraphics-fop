/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

/**
 * class used to create a PDF Uri link
 */
public class PDFUri extends PDFAction {


    String uri;

    /**
     * create a Uri instance.
     *
     * @param uri the uri to which the link should point
     */
    public PDFUri(String uri) {

        this.uri = uri;
    }

    /**
     * returns the action ncecessary for a uri
     *
     * @return the action to place next to /A within a Link
     */
    public String getAction() {
        return "<< /URI (" + uri + ")\n/S /URI >>";
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
