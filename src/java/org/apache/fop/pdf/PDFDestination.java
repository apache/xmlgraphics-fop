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

import org.apache.fop.area.DestinationData;
import org.apache.fop.area.PageViewport;

/**
 * class representing a named destination
 */
public class PDFDestination extends PDFObject {

    /**
     * PDFReference (object reference) for this destination
     */
     private String goToReference;

    /**
     * ID Reference for this destination
     */
    private String idRef;

    /**
     * PageViewport to which the idRef item refers
     */
    private PageViewport pageViewport = null;

    /**
     * create a named destination
     */
    public PDFDestination(DestinationData destinationData) {
        /* generic creation of PDF object */
        super();
        this.goToReference = destinationData.getGoToReference();
        this.idRef = destinationData.getIDRef();
        this.pageViewport = destinationData.getPageViewport();
    }

    /**
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    public String toPDFString() {
        String s = getObjectID()
                   + "<<"
                   + "/Limits [(" + idRef + ") (" + idRef + ")]\n"
                   + "/Names [(" + idRef + ") " + goToReference + "]"
                   + "\n>>\nendobj\n";
        return s;
    }

    /*
     * example:
     *
     * 249 0 obj
     * <<
     * /Limits [(drivervariables) (drivervariables)]
     * /Names [(drivervariables) 73 0 R]
     * >>
     * endobj
     */

    /**
     * Sets the GoToReference in the associated DestinationData object.
     *
     * @param goToReference the reference to set in the associated DestinationData object.
     */
    public void setGoToReference(String goToReference) {
        this.goToReference = goToReference;
    }

    /**
     * Returns the GoToReference from the associated DestinationData object.
     *
     * @return the GoToReference from the associated DestinationData object.
     */
    public String getGoToReference() {
        return this.goToReference;
    }

    /**
     * Get the PageViewport object that this destination refers to
     *
     * @return the PageViewport that this destination points to
     */
    public PageViewport getPageViewport() {
        return this.pageViewport;
    }

    /**
     * Returns the RefID from the associated DestinationData object.
     *
     * @return the RefID from the associated DestinationData object.
     */
    public String getIDRef() {
        return this.idRef;
    }

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

        if (obj == null || !(obj instanceof PDFDestination)) {
            return false;
        }

        PDFDestination dest = (PDFDestination)obj;
        if (dest.getIDRef() == this.getIDRef()) {
            return true;
        }
        
        return true;
    }
}

