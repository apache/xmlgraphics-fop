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

import org.xml.sax.Locator;

/**
 * Base class for representation of mixed content formatting objects
 * and their processing
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
                                 Locator locator) {
        if (textInfo == null) {
            // Really only need one of these, but need to get fontInfo
            // stored in propMgr for later use.
            propMgr.setFontInfo(getFOInputHandler().getFontInfo());
            textInfo = propMgr.getTextLayoutProps(getFOInputHandler().getFontInfo());
        }

        FOText ft = new FOText(data, start, length, textInfo, this);
        ft.setLocation(locator);
        ft.setName("text");
        
        /* characters() processing empty for FOTreeHandler, not empty for RTF & MIFHandlers */
        getFOInputHandler().characters(ft.ca, ft.startIndex, ft.endIndex);

        addChild(ft);
    }

    private void setup() {
        if (this.propertyList != null) {
            setupID();
        }
    }

    /**
     * @return iterator for this object
     */
    public CharIterator charIterator() {
        return new RecursiveCharIterator(this);
    }

    /**
     * This is a hook for an FOTreeVisitor subclass to be able to access
     * this object.
     * @param fotv the FOTreeVisitor subclass that can access this object.
     * @see org.apache.fop.fo.FOTreeVisitor
     */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveFObjMixed(this);
    }
}

