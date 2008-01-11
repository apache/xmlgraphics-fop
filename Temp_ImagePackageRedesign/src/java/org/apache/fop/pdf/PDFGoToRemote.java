/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * class representing a /GoToR object.
 */
public class PDFGoToRemote extends PDFAction {

    /**
     * the file specification
     */
    private PDFFileSpec pdfFileSpec;
    private int pageReference = 0;
    private String destination = null;

    /**
     * create an GoToR object.
     *
     * @param pdfFileSpec the fileSpec associated with the action
     */
    public PDFGoToRemote(PDFFileSpec pdfFileSpec) {
        /* generic creation of object */
        super();

        this.pdfFileSpec = pdfFileSpec;
    }

    /**
     * create an GoToR object.
     *
     * @param pdfFileSpec the fileSpec associated with the action
     * @param page a page reference within the remote document
     */
    public PDFGoToRemote(PDFFileSpec pdfFileSpec, int page) {
        /* generic creation of object */
        super();

        this.pdfFileSpec = pdfFileSpec;
        this.pageReference = page;
    }

    /**
     * create an GoToR object.
     *
     * @param pdfFileSpec the fileSpec associated with the action
     * @param dest a named destination within the remote document
     */
    public PDFGoToRemote(PDFFileSpec pdfFileSpec, String dest) {
        /* generic creation of object */
        super();

        this.pdfFileSpec = pdfFileSpec;
        this.destination = dest;
    }

    /**
     * return the action string which will reference this object
     *
     * @return the action String
     */
    public String getAction() {
        return this.referencePDF();
    }

    /**
     * {@inheritDoc}
     */
    public String toPDFString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append(getObjectID());
        sb.append("<<\n/S /GoToR\n/F " + pdfFileSpec.referencePDF() + "\n");

        if (destination != null) {
            sb.append("/D (" + this.destination + ")");
        } else {
            sb.append("/D [ " + this.pageReference + " /XYZ null null null ]");
        }

        sb.append(" \n>>\nendobj\n");

        return sb.toString();
    }


    /*
     * example
     * 28 0 obj
     * <<
     * /S /GoToR
     * /F 29 0 R
     * /D [ 0 /XYZ -6 797 null ]
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

        if (obj == null || !(obj instanceof PDFGoToRemote)) {
            return false;
        }

        PDFGoToRemote remote = (PDFGoToRemote)obj;

        if (!remote.pdfFileSpec.referencePDF().equals(pdfFileSpec.referencePDF())) {
            return false;
        }

        if (destination != null) {
            if (!destination.equals(remote.destination)) {
                return false;
            }
        } else {
            if (pageReference != remote.pageReference) {
                return false;
            }
        }

        return true;
    }
}

