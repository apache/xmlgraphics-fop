/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */
 
package org.apache.fop.pdf;

import java.util.Date;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
    private Date creationDate = null;

    /**
     * the name of the application that created the
     * original document before converting to PDF
     */
    private String creator;

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

    /** @return the title string */
    public String getTitle() {
        return this.title;
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
     * @return last set creation date
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param date Date to store in the PDF as creation date. Use null to force current system date.
     */
    public void setCreationDate(Date date) {
        creationDate = date;
    }

    /**
     * @see org.apache.fop.pdf.PDFObject#toPDF()
     */
    public byte[] toPDF() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(128);
        try {
            bout.write(encode(getObjectID()));
            bout.write(encode("<< /Type /Info\n"));
            if (title != null) {
                bout.write(encode("/Title "));
                bout.write(encodeText(this.title));
                bout.write(encode("\n"));
            }
            if (author != null) {
                bout.write(encode("/Author "));
                bout.write(encodeText(this.author));
                bout.write(encode("\n"));
            }
            if (subject != null) {
                bout.write(encode("/Subject "));
                bout.write(encodeText(this.subject));
                bout.write(encode("\n"));
            }
            if (keywords != null) {
                bout.write(encode("/Keywords "));
                bout.write(encodeText(this.keywords));
                bout.write(encode("\n"));
            }
    
            if (creator != null) {
                bout.write(encode("/Creator "));
                bout.write(encodeText(this.creator));
                bout.write(encode("\n"));
            }
    
            bout.write(encode("/Producer "));
            bout.write(encodeText(this.producer));
            bout.write(encode("\n"));
    
            // creation date in form (D:YYYYMMDDHHmmSSOHH'mm')
            if (creationDate == null) {
                creationDate = new Date();
            }
            bout.write(encode("/CreationDate "));
            bout.write(encodeString(formatDateTime(creationDate)));
            bout.write(encode("\n>>\nendobj\n"));
        } catch (IOException ioe) {
            log.error("Ignored I/O exception", ioe);
        }
        return bout.toByteArray();
    }

}

