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
 
package org.apache.fop.area;

// XML
import org.xml.sax.SAXException;

/**
 * This is the model for the area tree object.
 * The model implementation can handle the page sequence,
 * page and extensions.
 * The methods to access the page viewports can only
 * assume the PageViewport is valid as it remains for
 * the life of the area tree model.
 */
public abstract class AreaTreeModel {
    /**
     * Start a page sequence on this model.
     * @param title the title of the new page sequence
     */
    public abstract void startPageSequence(Title title);

    /**
     * Add a page to this moel.
     * @param page the page to add to the model.
     */
    public abstract void addPage(PageViewport page);

    /**
     * Add an extension to this model.
     * @param ext the extension to add
     * @param when when the extension should be handled
     */
    public abstract void addExtension(TreeExt ext, int when);

    /**
     * Signal the end of the document for any processing.
     */
    public abstract void endDocument() throws SAXException;

    /**
     * Get the page sequence count.
     * @return the number of page sequences in the document.
     */
    public abstract int getPageSequenceCount();

    /**
     * Get the page count.
     * @param seq the page sequence to count.
     * @return returns the number of pages in a page sequence
     */
    public abstract int getPageCount(int seq);

    /**
     * Get the page for a position in the document.
     * @param seq the page sequence number
     * @param count the page count in the sequence
     * @return the PageViewport for the particular page
     */
    public abstract PageViewport getPage(int seq, int count);
    
    /**
     * Get the title for a page sequence.
     * @param count the page sequence count
     * @return the title of the page sequence
     */
    public abstract Title getTitle(int count);

}
