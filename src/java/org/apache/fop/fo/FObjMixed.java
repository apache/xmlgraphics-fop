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

package org.apache.fop.fo;

import java.util.List;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.apache.fop.layoutmgr.LMiter;
import org.apache.fop.layoutmgr.InlineStackingLayoutManager;

/**
 * Base class for representation of mixed content formatting objects
 * and their processing
 * @todo define what a "mixed content formatting object" is
 */
public class FObjMixed extends FObj {
    /** TextInfo for this object */
    protected TextInfo textInfo = null;

    /**
     * @param parent FONode that is the parent of this object
     */
    public FObjMixed(FONode parent) {
        super(parent);
    }

    /**
     * @param data array of characters containing text to be added
     * @param start starting array element to add
     * @param length number of characters to add
     * @param locator location in fo source file. 
     */
    protected void addCharacters(char data[], int start, int length,
                                 Locator locator) throws SAXParseException {
        if (textInfo == null) {
            // Really only need one of these, but need to get fontInfo
            // stored in propMgr for later use.
            propMgr.setFontInfo(getFOEventHandler().getFontInfo());
            textInfo = propMgr.getTextLayoutProps(getFOEventHandler().getFontInfo());
        }

        FOText ft = new FOText(data, start, length, textInfo, this);
        ft.setLocator(locator);
        
        getFOEventHandler().characters(ft.ca, ft.startIndex, ft.endIndex);
        addChildNode(ft);
    }

    /**
     * @return iterator for this object
     */
    public CharIterator charIterator() {
        return new RecursiveCharIterator(this);
    }

    /**
     * @see org.apache.fop.fo.FObj#addLayoutManager(List)
     */
    public void addLayoutManager(List list) { 	 
        if (getChildNodes() != null) {
            InlineStackingLayoutManager lm;
            lm = new InlineStackingLayoutManager(this);
            list.add(lm);
        }
    }
    
}

