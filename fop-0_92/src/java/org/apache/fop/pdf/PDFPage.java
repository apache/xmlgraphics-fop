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

/**
 * Class representing a /Page object.
 * <p>
 * There is one of these for every page in a PDF document. The object
 * specifies the dimensions of the page and references a /Resources
 * object, a contents stream and the page's parent in the page
 * hierarchy.
 */
public class PDFPage extends PDFResourceContext {

    /**
     * Holds a reference on the parent PDFPages object.
     */
    private String parentRef;

    /**
     * the contents stream
     */
    protected PDFStream contents;

    /**
     * the width of the page in points
     */
    protected int pagewidth;

    /**
     * the height of the page in points
     */
    protected int pageheight;

    /** the page index (zero-based) */
    protected int pageIndex;
    
    /**
     * Duration to display page
     */
    protected int duration = -1;

    /**
     * Transition dictionary
     */
    protected TransitionDictionary trDictionary = null;

    /**
     * Create a /Page object
     *
     * @param resources the /Resources object
     * @param contents the content stream
     * @param pageWidth the page's width in points
     * @param pageHeight the page's height in points
     * @param pageIndex the page's zero-based index (or -1 if the page number is auto-determined)
     */
    public PDFPage(PDFResources resources, PDFStream contents,
                   int pageWidth, int pageHeight, int pageIndex) {

        /* generic creation of object */
        super(resources);

        /* set fields using parameters */
        this.contents = contents;
        this.pagewidth = pageWidth;
        this.pageheight = pageHeight;
        this.pageIndex = pageIndex;
    }

    /**
     * Create a /Page object
     *
     * @param resources the /Resources object
     * @param pageWidth the page's width in points
     * @param pageHeight the page's height in points
     * @param pageIndex the page's zero-based index (or -1 if the page number is auto-determined)
     */
    public PDFPage(PDFResources resources,
                   int pageWidth, int pageHeight, int pageIndex) {
        this(resources, null, pageWidth, pageHeight, pageIndex);
    }

    /**
     * set this page contents
     *
     * @param contents the contents of the page
     */
    public void setContents(PDFStream contents) {
        this.contents = contents;
    }

    /**
     * set this page's parent
     *
     * @param parent the /Pages object that is this page's parent
     */
    public void setParent(PDFPages parent) {
        this.parentRef = parent.referencePDF();
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
        duration = dur;
        trDictionary = tr;
    }

    /**
     * Returns the page width.
     * @return the page width
     */
    public int getWidth() {
        return this.pagewidth;
    }

    /**
     * Returns the page height.
     * @return the page height
     */
    public int getHeight() {
        return this.pageheight;
    }

    /**
     * @return the page Index of this page (zero-based), -1 if it the page index should
     *         automatically be determined.
     */
    public int getPageIndex() {
        return this.pageIndex;
    }
    
    /**
     * @see org.apache.fop.pdf.PDFObject#toPDFString()
     */
    public String toPDFString() {
        StringBuffer sb = new StringBuffer();

        sb = sb.append(getObjectID()
                       + "<< /Type /Page\n" 
                       + "/Parent " + this.parentRef + "\n"
                       + "/MediaBox [ 0 0 " + getWidth() + " "
                       + getHeight() + " ]\n" 
                       + "/Resources " + this.resources.referencePDF() + "\n" 
                       + "/Contents " + this.contents.referencePDF() + "\n");
        if (this.annotList != null) {
            sb = sb.append("/Annots " + this.annotList.referencePDF() + "\n");
        }
        if (this.duration != -1) {
            sb = sb.append("/Dur " + this.duration + "\n");
        }
        if (this.trDictionary != null) {
            sb = sb.append("/Trans << " + this.trDictionary.getDictionary() + " >>\n");
        }

        sb = sb.append(">>\nendobj\n");
        return sb.toString();
    }

}
