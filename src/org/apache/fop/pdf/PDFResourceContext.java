/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

/**
 * The PDF resource context.
 *
 * There is one of these for every page in a PDF document. The object
 * specifies the dimensions of the page and references a /Resources
 * object, a contents stream and the page's parent in the page
 * hierarchy.
 *
 * Modified by Mark Lillywhite, mark-fop@inomial.com. The Parent
 * object was being referred to by reference, but all that we
 * ever used from the Parent was it's PDF object ID, and according
 * to the memory profile this was causing OOM issues. So, we store
 * only the object ID of the parent, rather than the parent itself.
 */
public class PDFResourceContext extends PDFObject {

    /**
     * the page's /Resource object
     */
    protected PDFResources resources;

    /**
     * the list of annotation objects for this page
     */
    protected PDFAnnotList annotList;

    /**
     * Reference to document used when creating annotation list
     */
    protected PDFDocument document;

    /**
     *
     * @param number the object's number
     * @param doc the PDF document this belongs to
     * @param resources the /Resources object
     */
    public PDFResourceContext(int number, PDFDocument doc, PDFResources resources) {
        /* generic creation of object */
        super(number);

        /* set fields using parameters */
        this.document = doc;
        this.resources = resources;
        this.annotList = null;
    }

    /**
     * Get the resources for this resource context.
     *
     * @return the resources in this resource context
     */
    public PDFResources getPDFResources() {
        return this.resources;
    }

    /**
     * set this page's annotation list
     *
     * @param annot a PDFAnnotList list of annotations
     */
    public void addAnnotation(PDFObject annot) {
        if (this.annotList == null) {
            this.annotList = document.makeAnnotList();
        }
        this.annotList.addAnnot(annot);
    }

    /**
     * Get the current annotations.
     *
     * @return the current annotation list
     */
    public PDFAnnotList getAnnotations() {
        return this.annotList;
    }

    /**
     * A a GState to this resource context.
     *
     * @param gstate the GState to add
     */
    public void addGState(PDFGState gstate) {
        this.resources.addGState(gstate);
    }

    /**
     * Add the shading tot he current resource context.
     *
     * @param shading the shading to add
     */
    public void addShading(PDFShading shading) {
        this.resources.addShading(shading);
    }

    /**
     * Get the PDF, unused.
     *
     * @return null value
     */
    public byte[] toPDF() {
        return null;
    }
}
