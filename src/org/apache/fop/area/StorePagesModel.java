/*
 * $Id$
 * Copyright (C) 2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area;

// Java
import java.util.List;
import java.util.ArrayList;

/**
 * This class stores all the pages in the document
 * for interactive agents.
 * The pages are stored and can be retrieved in any order.
 */
public class StorePagesModel extends AreaTreeModel {
    private List pageSequence = null;
    private List titles = new ArrayList();
    private List currSequence;
    private List extensions = new ArrayList();

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
    public void startPageSequence(Title title) {
        titles.add(title);
        if (pageSequence == null) {
            pageSequence = new ArrayList();
        }
        currSequence = new ArrayList();
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
     * Get the title for a page sequence.
     * @param count the page sequence count
     * @return the title of the page sequence
     */
    public Title getTitle(int count) {
        return (Title) titles.get(count);
    }

    /**
     * Get the page count.
     * @param seq the page sequence to count.
     * @return returns the number of pages in a page sequence
     */
    public int getPageCount(int seq) {
        List sequence = (List) pageSequence.get(seq);
        return sequence.size();
    }

    /**
     * Get the page for a position in the document.
     * @param seq the page sequence number
     * @param count the page count in the sequence
     * @return the PageViewport for the particular page
     */
    public PageViewport getPage(int seq, int count) {
        List sequence = (List) pageSequence.get(seq);
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
     * End document, do nothing.
     */
    public void endDocument() {
    }
}
