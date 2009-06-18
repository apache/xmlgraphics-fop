/*
 *
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created on 10/05/2004
 * $Id$
 */
package org.apache.fop.area;

import java.util.ArrayList;

/**
 * A <code>PageList</code> is a sequential list of pages generated as part
 * of the process of building and rendering a <code>page-sequence</code>.
 * It may contain elements which implement the <code>PageListElement</code>
 * interface; i.e., <code>Pages</code>s or <code>PageSet</code>s.
 *  
 * @author pbw
 * @version $Revision$ $Name$
 */
public class PageList implements PageSetElement {

    /** The array of pages, randomly accessible */
    private ArrayList pagelist;
    /** The id of this list. As with <code>Page</code>s, 0 is not valid. */
    private final long id;
    /**
     * With a single element as an argument, the constructs a
     * pagelist with that element as its only contents
     * @param element
     */
    public PageList(PageListElement element) {
        pagelist = new ArrayList();
        pagelist.add(element);
        // Set the pagelist id immutably to the id of the first element
        id = element.getId();
    }
    /* (non-Javadoc)
     * @see org.apache.fop.area.PageSetElement#isPageList()
     */
    public boolean isPageList() {
        return true;
    }

    public long getId() {
        return id;
    }
    /**
     * Gets the size of the list
     * @return the size
     */
    public synchronized int size() {
        return pagelist.size();
    }
    /**
     * Gets the <code>i</code>th <code>PageListElement</code> of the list
     * @param i the index of the element to get
     * @return the indexed element
     * @throws IndexOutOfBoundsException
     */
    public synchronized PageListElement get(int i)
    throws IndexOutOfBoundsException {
        return (PageListElement)(pagelist.get(i));
    }
    /**
     * Appends the <code>PageListElement</code> to the list
     * @param element to append
     */
    public synchronized void append(PageListElement element) {
        pagelist.add(element);
    }
    /**
     * Inserts the given <code>PageListElement</code> at the given position
     * @param insertPos the insert position
     * @param element to insert
     */
    public synchronized void insert(int insertPos, PageListElement element) {
        pagelist.add(insertPos, element);
    }
    /**
     * Romoves the element at the given index position
     * @param elementPos
     * @return the <code>PageListElement</code> removed
     */
    public synchronized PageListElement remove(int elementPos) {
        return (PageListElement)(pagelist.remove(elementPos));
    }
}
