/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.area;

// Java
import java.util.List;

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
    public void startPageSequence(Title title) {
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
