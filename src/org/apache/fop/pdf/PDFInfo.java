/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
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
    protected String producer;

    protected String title = null;
    protected String author = null;
    protected String subject = null;
    protected String keywords = null;

    // the name of the application that created the
    // original document before converting to PDF
    protected String creator;

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

    public void setTitle(String t) {
        this.title = t;
    }

    public void setAuthor(String a) {
        this.author = a;
    }

    public void setSubject(String s) {
        this.subject = s;
    }

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
        if(title != null) {
            p += "/Title (" + this.title + ")\n";
        }
        if(author != null) {
            p += "/Author (" + this.author + ")\n";
        }
        if(subject != null) {
            p += "/Subject (" + this.subject + ")\n";
        }
        if(keywords != null) {
            p += "/Keywords (" + this.keywords + ")\n";
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

