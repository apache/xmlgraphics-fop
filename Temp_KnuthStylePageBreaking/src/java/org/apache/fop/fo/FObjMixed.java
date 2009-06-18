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

package org.apache.fop.fo;

import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;

/**
 * Base class for representation of mixed content formatting objects
 * (i.e., those that can contain both child FO's and text nodes/PCDATA).
 * It should not be instantiated directly.
 */
public abstract class FObjMixed extends FObj {
    /**
     * @param parent FONode that is the parent of this object
     */
    protected FObjMixed(FONode parent) {
        super(parent);
    }

    /**
     * Adds characters
     * @param data array of characters containing text to be added
     * @param start starting array element to add
     * @param end ending array element to add
     * @param pList currently applicable PropertyList 
     * @param locator location in fo source file.
     * @throws FOPException if there's a problem during processing
     * @see org.apache.fop.fo.FONode#addCharacters(char[], int, int, org.apache.fop.fo.PropertyList, org.xml.sax.Locator)
     */
    protected void addCharacters(char[] data, int start, int end,
                                 PropertyList pList,
                                 Locator locator) throws FOPException {
        FOText ft = new FOText(data, start, end, this);
        ft.setLocator(locator);
        ft.bind(pList);
        ft.startOfNode();
        
        getFOEventHandler().characters(ft.ca, ft.startIndex, ft.endIndex);
        addChildNode(ft);
    }

    /**
     * @return iterator for this object
     */
    public CharIterator charIterator() {
        return new RecursiveCharIterator(this);
    }
    
}

