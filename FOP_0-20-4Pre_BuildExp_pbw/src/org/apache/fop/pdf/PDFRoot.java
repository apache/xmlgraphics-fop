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
     * create a Root (/Catalog) object. NOTE: The PDFRoot
     * object must be created before the PDF document is
     * generated, but it is not assigned an object ID until
     * it is about to be written (immediately before the xref
     * table as part of the trsailer). (mark-fop@inomial.com)
     *
     * @param number the object's number
     */
    public PDFRoot(int number, PDFPages pages) {
        super(number);
        setRootPages(pages);
    }

    /**
     * Before the root is written to the document stream,
     * make sure it's object number is set. Package-private access
     * only; outsiders should not be fiddling with this stuff.
     */
    void setNumber(int number) {
        this.number = number;
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

    public PDFOutline getRootOutline() {
        return _outline;
    }


    /**
     * represent the object as PDF.
     *
     * @throws IllegalStateException if the setNumber() method has
     * not been called.
     *
     * @return the PDF string
     */
    public byte[] toPDF()
    throws IllegalStateException {
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
