/*
 * $Id$
 * Copyright (C) 2001-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * class representing an /Info object
 */
public class PDFInfo extends PDFObject {

    /**
     * the application producing the PDF
     */
    private String producer;

    private String title = null;
    private String author = null;
    private String subject = null;
    private String keywords = null;

    // the name of the application that created the
    // original document before converting to PDF
    private String creator;

    /**
     * create an Info object
     *
     * @param number the object's number
     */
    public PDFInfo(int number) {
        super(number);
    }

    /**
     * set the producer string
     *
     * @param producer the producer string
     */
    public void setProducer(String producer) {
        this.producer = producer;
    }

    /**
     * set the creator string
     *
     * @param creator the document creator
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * set the title string
     *
     * @param t the document title
     */
    public void setTitle(String t) {
        this.title = t;
    }

    /**
     * set the author string
     *
     * @param a the document author
     */
    public void setAuthor(String a) {
        this.author = a;
    }

    /**
     * set the subject string
     *
     * @param s the document subject
     */
    public void setSubject(String s) {
        this.subject = s;
    }

    /**
     * set the keywords string
     *
     * @param k the keywords for this document
     */
    public void setKeywords(String k) {
        this.keywords = k;
    }

    /**
     * produce the PDF representation of the object
     *
     * @return the PDF
     */
    public byte[] toPDF() {
        String p = this.number + " " + this.generation
                   + " obj\n<< /Type /Info\n";
        if (title != null) {
            p += "/Title (" + this.title + ")\n";
        }
        if (author != null) {
            p += "/Author (" + this.author + ")\n";
        }
        if (subject != null) {
            p += "/Subject (" + this.subject + ")\n";
        }
        if (keywords != null) {
            p += "/Keywords (" + this.keywords + ")\n";
        }

        if (creator != null) {
            p += "/Creator (" + this.creator + ")\n";
        }

        p += "/Producer (" + this.producer + ")\n";

        // creation date in form (D:YYYYMMDDHHmmSSOHH'mm')
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        String str = sdf.format(date) + "+00'00'";
        p += "/CreationDate (D:" + str + ")";
        p += " >>\nendobj\n";
        return p.getBytes();
    }
}

