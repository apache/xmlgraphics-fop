/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

/**
 * class representing a /FileSpec object.
 *
 */
public class PDFFileSpec extends PDFObject {

    /**
     * the filename
     */
    protected String filename;

    /**
     * create a /FileSpec object.
     *
     * @param filename the filename represented by this object
     */
    public PDFFileSpec(String filename) {

        /* generic creation of object */
        super();

        this.filename = filename;
    }

    /**
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    public String toPDFString() {
        return getObjectID() 
                + "<<\n/Type /FileSpec\n" 
                + "/F (" + this.filename + ")\n" 
                + ">>\nendobj\n";
    }

    /*
     * example
     * 29 0 obj
     * <<
     * /Type /FileSpec
     * /F (table1.pdf)
     * >>
     * endobj
     */

    /**
     * Check if this equals another object.
     *
     * @param obj the object to compare
     * @return true if this equals other object
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof PDFFileSpec)) {
            return false;
        }

        PDFFileSpec spec = (PDFFileSpec)obj;

        if (!spec.filename.equals(filename)) {
            return false;
        }

        return true;
    }
}

