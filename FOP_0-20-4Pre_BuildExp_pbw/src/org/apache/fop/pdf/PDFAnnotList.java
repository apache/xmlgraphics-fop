/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

// Java
import java.util.Vector;

/**
 * class representing an object which is a list of annotations.
 *
 * This PDF object is a list of references to /Annot objects. So far we
 * are dealing only with links.
 */
public class PDFAnnotList extends PDFObject {

    /**
     * the /Annot objects
     */
    protected Vector links = new Vector();

    /**
     * the number of /Annot objects
     */
    protected int count = 0;

    /**
     * create a /Annots object.
     *
     * @param number the object's number
     */
    public PDFAnnotList(int number) {

        /* generic creation of object */
        super(number);
    }

    /**
     * add an /Annot object of /Subtype /Link.
     *
     * @param link the PDFLink to add.
     */
    public void addLink(PDFLink link) {
        this.links.addElement(link);
        this.count++;
    }

    /**
     * get the count of /Annot objects
     *
     * @return the number of links
     */
    public int getCount() {
        return this.count;
    }

    /**
     * represent the object in PDF
     *
     * @return the PDF string
     */
    public byte[] toPDF() {
        StringBuffer p = new StringBuffer(this.number + " " + this.generation
                                          + " obj\n[\n");
        for (int i = 0; i < this.count; i++) {
            p = p.append(((PDFObject)links.elementAt(i)).referencePDF()
                         + "\n");
        }
        p = p.append("]\nendobj\n");
        return p.toString().getBytes();
    }

    /*
     * example
     * 20 0 obj
     * [
     * 19 0 R
     * ]
     * endobj
     */
}
