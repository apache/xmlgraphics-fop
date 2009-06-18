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
 * class representing a /GoTo object.
 * This can either have a Goto to a page reference and location
 * or to a specified PDF reference string.
 */
public class PDFGoTo extends PDFAction {

    /**
     * the pageReference
     */
    private String pageReference;
    private String destination = null;
    private float xPosition = 0;
    private float yPosition = 0;

    /**
     * create a /GoTo object.
     *
     * @param pageReference the pageReference represented by this object
     */
    public PDFGoTo(String pageReference) {
        /* generic creation of object */
        super();

        this.pageReference = pageReference;
    }

    /**
     * Sets page reference after object has been created
     *
     * @param pageReference the new page reference to use
     */
    public void setPageReference(String pageReference) {
        this.pageReference = pageReference;
    }

    /**
     * Sets the Y position to jump to
     *
     * @param yPosition y position
     */
    public void setYPosition(float yPosition) {
        this.yPosition = yPosition;
    }

    /**
     * Set the destination string for this Goto.
     *
     * @param dest the PDF destination string
     */
    public void setDestination(String dest) {
        destination = dest;
    }

    /**
     * Sets the x Position to jump to
     *
     * @param xPosition x position
     */
    public void setXPosition(int xPosition) {
        this.xPosition = (xPosition / 1000f);
    }

    /**
     * Get the PDF reference for the GoTo action.
     *
     * @return the PDF reference for the action
     */
    public String getAction() {
        return referencePDF();
    }

    /**
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    public String toPDFString() {
        String dest;
        if (destination == null) {
            dest = "/D [" + this.pageReference + " /XYZ " + xPosition
                          + " " + yPosition + " null]\n";
        } else {
            dest = "/D [" + this.pageReference + " " + destination + "]\n";
        }
        return getObjectID() 
                    + "<< /Type /Action\n/S /GoTo\n" + dest
                    + ">>\nendobj\n";
    }

    /*
     * example
     * 29 0 obj
     * <<
     * /S /GoTo
     * /D [23 0 R /FitH 600]
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

        if (obj == null || !(obj instanceof PDFGoTo)) {
            return false;
        }

        PDFGoTo gt = (PDFGoTo)obj;

        if (gt.pageReference == null) {
            if (pageReference != null) {
                return false;
            }
        } else {
            if (!gt.pageReference.equals(pageReference)) {
                return false;
            }
        }

        if (destination == null) {
            if (!(gt.destination == null && gt.xPosition == xPosition
                && gt.yPosition == yPosition)) {
                return false;
            }
        } else {
            if (!destination.equals(gt.destination)) {
                return false;
            }
        }

        return true;
    }
}

