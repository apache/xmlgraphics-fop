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

// Java
import java.util.List;

// XML
import org.xml.sax.SAXException;


/**
 * This class stores all the pages in the document
 * for interactive agents.
 * The pages are stored and can be retrieved in any order.
 */
public class StorePagesModel extends AreaTreeModel {
    private List pageSequence = null;
    private List titles = new java.util.ArrayList();
    private List currSequence;
    private List extensions = new java.util.ArrayList();

    /**
     * Create a new store pages model
     */
    public StorePagesModel() {
    }

    /**
     * Start a new page sequence.
     * This creates a new list for the pages in the new page sequence.
     * @param title the title of the page sequence.
     */
    public void startPageSequence(LineArea title) {
        titles.add(title);
        if (pageSequence == null) {
            pageSequence = new java.util.ArrayList();
        }
        currSequence = new java.util.ArrayList();
        pageSequence.add(currSequence);
    }

    /**
     * Add a page.
     * @param page the page to add to the current page sequence
     */
    public void addPage(PageViewport page) {
        currSequence.add(page);
    }

    /**
     * Get the page sequence count.
     * @return the number of page sequences in the document.
     */
    public int getPageSequenceCount() {
        return pageSequence.size();
    }

    /**
     * Get the page count.
     * @param seq the page sequence to count.
     * @return returns the number of pages in a page sequence
     */
    public int getPageCount(int seq) {
        List sequence = (List) pageSequence.get(seq - 1);
        return sequence.size();
    }

    /**
     * Get the page for a position in the document.
     * @param seq the page sequence number
     * @param count the page count in the sequence
     * @return the PageViewport for the particular page
     */
    public PageViewport getPage(int seq, int count) {
        List sequence = (List) pageSequence.get(seq - 1);
        return (PageViewport) sequence.get(count);
    }

    /**
     * Add an extension to the store page model.
     * The extension is stored so that it can be retrieved in the
     * appropriate position.
     * @param ext the extension to add
     * @param when when the extension should be handled
     */
    public void addExtension(TreeExt ext, int when) {
        int seq, page;
        switch(when) {
            case TreeExt.IMMEDIATELY:
                seq = pageSequence == null ? 0 : pageSequence.size();
                page = currSequence == null ? 0 : currSequence.size();
                break;
            case TreeExt.AFTER_PAGE:
                break;
            case TreeExt.END_OF_DOC:
                break;
        }
        extensions.add(ext);
    }

    /**
     * Get the list of extensions that apply at a particular
     * position in the document.
     * @param seq the page sequence number
     * @param count the page count in the sequence
     * @return the list of extensions
     */
    public List getExtensions(int seq, int count) {
        return null;
    }

    /**
     * Get the end of document extensions for this stroe pages model.
     * @return the list of end extensions
     */
    public List getEndExtensions() {
        return extensions;
    }

    /**
     * Get the title for a page sequence.    
     * @param count the page sequence count      
     * @return the title of the page sequence    
     */      
    public LineArea getTitle(int count) {   
        return (LineArea) titles.get(count);
    }
     
    /**
     * End document, do nothing.
     */
    public void endDocument() throws SAXException {
    }
}
