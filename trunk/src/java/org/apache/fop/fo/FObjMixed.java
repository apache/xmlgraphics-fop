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
    
    /** Represents accumulated, pending FO text. See flushText(). */
    protected FOText ft = null;
    
    /**
     * @param parent FONode that is the parent of this object
     */
    protected FObjMixed(FONode parent) {
        super(parent);
    }

    /** @see org.apache.fop.fo.FONode */
    protected void addCharacters(char[] data, int start, int end,
                                 PropertyList pList,
                                 Locator locator) throws FOPException {
        if (ft == null) {
            ft = new FOText(this);
            ft.setLocator(locator);
            ft.bind(pList);
        }
        ft.addCharacters(data, start, end, null, null);
    }

    /** @see org.apache.fop.fo.FONode#endOfNode() */
    protected void endOfNode() throws FOPException {
        flushText();
        super.endOfNode();
    }

    /**
     * Adds accumulated text as one FOText instance.
     * Makes sure that nested calls to itself do nothing.
     * @throws FOPException if there is a problem during processing
     */
    protected void flushText() throws FOPException {
       if (ft != null) {
            FOText lft = ft;
            ft = null;
            lft.endOfNode();
            getFOEventHandler().characters(lft.ca, lft.startIndex, lft.endIndex);
            addChildNode(lft);
        }
    }

    protected void addChildNode(FONode child) throws FOPException {
        flushText();
        super.addChildNode(child);
    }

    /**
     * @return iterator for this object
     */
    public CharIterator charIterator() {
        return new RecursiveCharIterator(this);
    }
    
}

