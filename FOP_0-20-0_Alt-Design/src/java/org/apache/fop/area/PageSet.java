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
 * A <code>PageSet</code> is a set of <code>Page</code>s or
 * <code>PageList</code>s created from a common starting point in the FO tree.
 * That is, the first page of each member of a pageset shares a common
 * <code>page-sequence</code> and page number, and was initiated by the
 * same nested set of Flow Objects in the FO tree.  <code>PageSet</code> makes
 * no attempt to impose the usual set condition of no duplicated elements on
 * the contents of the set.
 * <p><code>PageSet</code>s are a means of representing multiple attempts to
 * lay out a section of the FO tree.
 * @author pbw
 * @version $Revision$ $Name$
 */
public class PageSet implements PageListElement {

    private ArrayList pageset;
    /** The id of this list. As with <code>Page</code>s, 0 is not valid. */
    private final long id;
    private int currElement = 0;

    public PageSet(PageSetElement element) {
        pageset = new ArrayList();
        pageset.add(element);
        // Set the pageset id immutably to the id of the first element
        id = element.getId();
    }
    /* (non-Javadoc)
     * @see org.apache.fop.area.PageListElement#isPageSet()
     */
    public boolean isPageSet() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.fop.area.PageListElement#getId()
     */
    public long getId() {
        return id;
    }

    /**
     * Get the index of the current element
     * @return the index
     */
    public int getCurrentElement() {
        return currElement;
    }

    /**
     * Sets the current element of the set
     * @param elindex the index of the element to set
     * @throws IndexOutOfBoundsException if <code>elindex</code> put of bounds
     */
    public void setCurrentElement(int elindex)
    throws IndexOutOfBoundsException {
        if (elindex < 0 || elindex >= pageset.size()) {
            throw new IndexOutOfBoundsException(
                    "Index " + elindex + "; size " + pageset.size());
        }
    }
    /**
     * Gets the element at the specified index position
     * @param index position of the element to get
     * @return the element
     */
    public PageSetElement get(int index) {
        return (PageSetElement)(pageset.get(index));
    }

    /**
     * Appends an element to the array
     * @param element to be appended
     */
    public void add(PageSetElement element) {
        pageset.add(element);
    }

    /**
     * Inserts an element at the indicated position in the array
     * @param insertPos the insert position
     * @param element to be inserted
     * @throws IndexOutOfBoundsException
     */
    public void insert(int insertPos, PageSetElement element)
    throws IndexOutOfBoundsException {
        pageset.add(insertPos, element);
    }

    /**
     * Removes the element at the specified index position in the array
     * @param index the position of the element to be removed
     * @return the removed <code>PageSetElement</code>
     * @throws IndexOutOfBoundsException
     */
    public PageSetElement remove(int index)
    throws IndexOutOfBoundsException {
        return (PageSetElement)(pageset.remove(index));
    }
}
