/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

// Java
import java.io.IOException;
import java.io.PrintWriter;

/**
 * class representing a Root (/Catalog) object
 */
public class PDFRoot extends PDFObject {

    /**
     * the /Pages object that is root of the Pages hierarchy
     */
    protected PDFPages rootPages;

    /**
     * Root outline object
     */
    private PDFOutline _outline;

    /**
     * create a Root (/Catalog) object
     *
     * @param number the object's number
     */
    public PDFRoot(int number) {
        super(number);
    }

    /**
     * add a /Page object to the root /Pages object
     *
     * @param page the /Page object to add
     */
    public void addPage(PDFPage page) {
        this.rootPages.addPage(page);
    }

    /**
     * set the root /Pages object
     *
     * @param pages the /Pages object to set as root
     */
    public void setRootPages(PDFPages pages) {
        this.rootPages = pages;
    }

    public void setRootOutline(PDFOutline outline) {
        _outline = outline;
    }


    /**
     * represent the object as PDF
     *
     * @return the PDF string
     */
    public byte[] toPDF() {
        StringBuffer p = new StringBuffer(this.number + " " + this.generation
                                          + " obj\n<< /Type /Catalog\n/Pages "
                                          + this.rootPages.referencePDF()
                                          + "\n");
        if (_outline != null) {
            p.append(" /Outlines " + _outline.referencePDF() + "\n");
            p.append(" /PageMode /UseOutlines\n");

        }
        p.append(" >>\nendobj\n");
        return p.toString().getBytes();
    }

}
