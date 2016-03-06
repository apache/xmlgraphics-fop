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

import java.awt.geom.Rectangle2D;

/**
 * Class representing a /Page object.
 * <p>
 * There is one of these for every page in a PDF document. The object
 * specifies the dimensions of the page and references a /Resources
 * object, a contents stream and the page's parent in the page
 * hierarchy.
 */
public class PDFPage extends PDFResourceContext {

    /** the page index (zero-based) */
    protected int pageIndex;

    /**
     * Create a /Page object
     *
     * @param resources the /Resources object
     * @param pageIndex the page's zero-based index (or -1 if the page number is auto-determined)
     * @param mediaBox the MediaBox
     * @param cropBox the CropBox. If null, mediaBox is used.
     * @param bleedBox the BleedBox. If null, cropBox is used.
     * @param trimBox the TrimBox. If null, bleedBox is used.
     */
    public PDFPage(PDFResources resources, int pageIndex,
                   Rectangle2D mediaBox, Rectangle2D cropBox,
                   Rectangle2D bleedBox, Rectangle2D trimBox) {
      /* generic creation of object */
      super(resources);

      put("Type", new PDFName("Page"));
      /* set fields using parameters */
      setSimplePageSize(mediaBox, cropBox, bleedBox, trimBox);
      this.pageIndex = pageIndex;
    }

    private void setSimplePageSize(Rectangle2D mediaBox, Rectangle2D cropBox,
                                   Rectangle2D bleedBox, Rectangle2D trimBox) {
        setMediaBox(mediaBox);

        if (cropBox == null) {
            cropBox = mediaBox;
        }
        setCropBox(cropBox);

        if (bleedBox == null) {
            bleedBox = cropBox;
        }
        setBleedBox(bleedBox); //Recommended by PDF/X

        if (trimBox == null) {
            trimBox = bleedBox;
        }
        setTrimBox(trimBox); //Needed for PDF/X
    }

    private PDFArray toPDFArray(Rectangle2D box) {
        return new PDFArray(this, new double[] {
                box.getX(), box.getY(), box.getMaxX(), box.getMaxY()});
    }

    /**
     * Sets the "MediaBox" entry
     * @param box the media rectangle
     */
    public void setMediaBox(Rectangle2D box) {
        put("MediaBox", toPDFArray(box));
    }

    /**
     * Sets the "CropBox" entry
     * @param box the bleed rectangle
     */
    public void setCropBox(Rectangle2D box) {
        put("CropBox", toPDFArray(box));
    }

    /**
     * Sets the "BleedBox" entry
     * @param box the bleed rectangle
     */
    public void setBleedBox(Rectangle2D box) {
        put("BleedBox", toPDFArray(box));
    }

    /**
     * Sets the "TrimBox" entry
     * @param box the trim rectangle
     */
    public void setTrimBox(Rectangle2D box) {
        put("TrimBox", toPDFArray(box));
    }

    /**
     * set this page contents
     *
     * @param contents the contents of the page
     */
    public void setContents(PDFStream contents) {
        if (contents != null) {
            put("Contents", new PDFReference(contents));
        }
    }

    /**
     * set this page's parent
     *
     * @param parent the /Pages object that is this page's parent
     */
    public void setParent(PDFPages parent) {
        put("Parent", new PDFReference(parent));
    }

    /**
     * Set the transition dictionary and duration.
     * This sets the duration of the page and the transition
     * dictionary used when going to the next page.
     *
     * @param dur the duration in seconds
     * @param tr the transition dictionary
     */
    public void setTransition(int dur, TransitionDictionary tr) {
        put("Dur", new Integer(dur));
        put("Trans", tr);
    }

    /**
     * @return the page Index of this page (zero-based), -1 if it the page index should
     *         automatically be determined.
     */
    public int getPageIndex() {
        return this.pageIndex;
    }

    /**
     * Sets the "StructParents" value.
     * @param structParents the integer key of this object's entry in the structural parent tree.
     */
    public void setStructParents(int structParents) {
        put("StructParents", structParents);
        //This is a PDF 1.5 feature. It is set as a work-around for a bug in Adobe Acrobat
        //which reports this missing even if the PDF file is PDF 1.4.
        setTabs(new PDFName("S"));
    }

    /**
     * Returns the value of the StructParents entry.
     *
     * @return the StructParents value, <code>null</code> if the entry has not been set
     */
    public Integer getStructParents() {
        return (Integer) get("StructParents");
    }

    /**
     * Specifies the tab order for annotations on a page.
     * @param value one of the allowed values (see PDF 1.5)
     * @since PDF 1.5
     */
    public void setTabs(PDFName value) {
        put("Tabs", value);
    }

}
