/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

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
    private PDFOutline outline;

    /**
     * create a Root (/Catalog) object. NOTE: The PDFRoot
     * object must be created before the PDF document is
     * generated, but it is not assigned an object ID until
     * it is about to be written (immediately before the xref
     * table as part of the trsailer). (mark-fop@inomial.com)
     *
     * @param number the object's number
     * @param pages the PDFPages object
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
    /*void setNumber(int number) {
        this.number = number;
    }*/

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

    /**
     * Set the root outline for the PDF document.
     *
     * @param out the root PDF Outline
     */
    public void setRootOutline(PDFOutline out) {
        outline = out;
    }

    /**
     * Get the root PDF outline for the document.
     *
     * @return the root PDF Outline
     */
    public PDFOutline getRootOutline() {
        return outline;
    }

    /**
     * represent the object as PDF.
     *
     * @return the PDF string
     */
    public byte[] toPDF() {
        StringBuffer p = new StringBuffer(this.number + " " + this.generation
                                          + " obj\n<< /Type /Catalog\n/Pages "
                                          + this.rootPages.referencePDF()
                                          + "\n");
        if (outline != null) {
            p.append(" /Outlines " + outline.referencePDF() + "\n");
            p.append(" /PageMode /UseOutlines\n");
        }
        p.append(" /PageMode /FullScreen\n");
        p.append(" >>\nendobj\n");
        return p.toString().getBytes();
    }

}
