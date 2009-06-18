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

import java.util.List;

/**
 * class representing a Root (/Catalog) object
 */
public class PDFRoot extends PDFObject {

    /**
     * Use no page mode setting, default
     */
    public static final int PAGEMODE_USENONE = 0;

    /**
     * Use outlines page mode to show bookmarks
     */
    public static final int PAGEMODE_USEOUTLINES = 1;

    /**
     * Use thumbs page mode to show thumbnail images
     */
    public static final int PAGEMODE_USETHUMBS = 2;

    /**
     * Full screen page mode
     */
    public static final int PAGEMODE_FULLSCREEN = 3;

    /**
     * the /Pages object that is root of the Pages hierarchy
     */
    protected PDFPages rootPages;

    /**
     * Root outline object
     */
    private PDFOutline outline;

    /** Optional Metadata object */
    private PDFMetadata metadata;
    
    /** The array of OutputIntents */
    private List outputIntents;
    
    private int pageMode = PAGEMODE_USENONE;

    /**
     * create a Root (/Catalog) object. NOTE: The PDFRoot
     * object must be created before the PDF document is
     * generated, but it is not assigned an object ID until
     * it is about to be written (immediately before the xref
     * table as part of the trsailer). (mark-fop@inomial.com)
     *
     * @param objnum the object's number
     * @param pages the PDFPages object
     */
    public PDFRoot(int objnum, PDFPages pages) {
        super();
        setObjectNumber(objnum);
        setRootPages(pages);
    }

    /**
     * Set the page mode for the PDF document.
     *
     * @param mode the page mode
     */
    public void setPageMode(int mode) {
        pageMode = mode;
    }

    /**
     * add a /Page object to the root /Pages object
     *
     * @param page the /Page object to add
     */
    public void addPage(PDFPage page) {
        this.rootPages.addPage(page);
    }

    /**
     * set the root /Pages object
     *
     * @param pages the /Pages object to set as root
     */
    public void setRootPages(PDFPages pages) {
        this.rootPages = pages;
    }

    /**
     * Set the root outline for the PDF document.
     *
     * @param out the root PDF Outline
     */
    public void setRootOutline(PDFOutline out) {
        outline = out;
    }

    /**
     * Get the root PDF outline for the document.
     *
     * @return the root PDF Outline
     */
    public PDFOutline getRootOutline() {
        return outline;
    }
    
    /**
     * Set the optional Metadata object.
     * @param meta the Metadata object
     * @since PDF 1.4
     */
    public void setMetadata(PDFMetadata meta) {
        this.metadata = meta;
    }
    
    /**
     * @return the Metadata object if set, null otherwise.
     * @since PDF 1.4
     */
    public PDFMetadata getMetadata() {
        return this.metadata;
    }

    /**
     * Adds an OutputIntent to the PDF
     * @param outputIntent the OutputIntent dictionary
     */
    public void addOutputIntent(PDFOutputIntent outputIntent) {
        if (this.outputIntents == null) {
            this.outputIntents = new java.util.ArrayList();
        }
        this.outputIntents.add(outputIntent);
    }
    
    /**
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    public String toPDFString() {
        StringBuffer p = new StringBuffer(128);
        p.append(getObjectID());
        p.append("<< /Type /Catalog\n/Pages "
                + this.rootPages.referencePDF()
                + "\n");
        if (outline != null) {
            p.append(" /Outlines " + outline.referencePDF() + "\n");
            p.append(" /PageMode /UseOutlines\n");
        } else {
            switch (pageMode) {
                case PAGEMODE_USEOUTLINES:
                    p.append(" /PageMode /UseOutlines\n");
                break;
                case PAGEMODE_USETHUMBS:
                    p.append(" /PageMode /UseThumbs\n");
                break;
                case PAGEMODE_FULLSCREEN:
                    p.append(" /PageMode /FullScreen\n");
                break;
                case PAGEMODE_USENONE:
                default:
                break;
            }
        }
        if (getMetadata() != null 
                && getDocumentSafely().getPDFVersion() >= PDFDocument.PDF_VERSION_1_4) {
            p.append("/Metadata " + getMetadata().referencePDF() + "\n");
        }
        if (this.outputIntents != null 
                && this.outputIntents.size() > 0
                && getDocumentSafely().getPDFVersion() >= PDFDocument.PDF_VERSION_1_4) {
            p.append("/OutputIntents [");
            for (int i = 0, c = this.outputIntents.size(); i < c; i++) {
                PDFOutputIntent outputIntent = (PDFOutputIntent)this.outputIntents.get(i);
                if (i > 0) {
                    p.append(" ");
                }
                p.append(outputIntent.referencePDF());
            }
            p.append("]\n");
        }
        p.append(">>\nendobj\n");
        return p.toString();
    }

}
