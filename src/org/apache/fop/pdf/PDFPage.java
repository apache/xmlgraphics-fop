/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

/**
 * class representing a /Page object.
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
public class PDFPage extends PDFObject {

    /**
     * the page's parent, a PDF reference object
     */
    protected String parent;

    /**
     * the page's /Resource object
     */
    protected PDFResources resources;

    /**
     * the contents stream
     */
    protected PDFStream contents;

    /**
     * the width of the page in points
     */
    protected int pagewidth;

    /**
     * the height of the page in points
     */
    protected int pageheight;

    /**
     * the list of annotation objects for this page
     */
    protected PDFAnnotList annotList;

    /**
     * create a /Page object
     *
     * @param number the object's number
     * @param resources the /Resources object
     * @param contents the content stream
     * @param pagewidth the page's width in points
     * @param pageheight the page's height in points
     */
    public PDFPage(int number, PDFResources resources, PDFStream contents,
                   int pagewidth, int pageheight) {

        /* generic creation of object */
        super(number);

        /* set fields using parameters */
        this.resources = resources;
        this.contents = contents;
        this.pagewidth = pagewidth;
        this.pageheight = pageheight;

        this.annotList = null;
    }

    /**
     * create a /Page object
     *
     * @param number the object's number
     * @param resources the /Resources object
     * @param pagewidth the page's width in points
     * @param pageheight the page's height in points
     */
    public PDFPage(int number, PDFResources resources,
                   int pagewidth, int pageheight) {

        /* generic creation of object */
        super(number);

        /* set fields using parameters */
        this.resources = resources;
        this.pagewidth = pagewidth;
        this.pageheight = pageheight;

        this.annotList = null;
    }

    /**
     * set this page contents
     * 
     * @param contents the contents of the page
     */
    public void setContents(PDFStream contents) {
        this.contents = contents;
    }

    /**
     * set this page's parent
     *
     * @param parent the /Pages object that is this page's parent
     */
    public void setParent(PDFPages parent) {
        this.parent = parent.referencePDF();
    }

    /**
     * set this page's annotation list
     *
     * @param annotList a PDFAnnotList list of annotations
     */
    public void setAnnotList(PDFAnnotList annotList) {
        this.annotList = annotList;
    }

    /**
     * get this page's annotation list
     *
     * @return annotList a PDFAnnotList list of annotations
     */
    public PDFAnnotList getAnnotList() {
        return this.annotList;
    }

    public void addShading(PDFShading shading) {
        this.resources.addShading(shading);
    }

    /**
     * represent this object as PDF
     *
     * @return the PDF string
     */
    public byte[] toPDF() {
        StringBuffer sb = new StringBuffer();

        sb = sb.append(this.number + " " + this.generation + " obj\n"
                       + "<< /Type /Page\n" + "/Parent "
                       + this.parent + "\n"
                       + "/MediaBox [ 0 0 " + this.pagewidth + " "
                       + this.pageheight + " ]\n" + "/Resources "
                       + this.resources.referencePDF() + "\n" + "/Contents "
                       + this.contents.referencePDF() + "\n");
        if (this.annotList != null) {
            sb = sb.append("/Annots " + this.annotList.referencePDF() + "\n");
        }

        sb = sb.append(">>\nendobj\n");

        return sb.toString().getBytes();
    }

}
