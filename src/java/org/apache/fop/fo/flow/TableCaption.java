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

package org.apache.fop.fo.flow;

// XML
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;

/**
 * Class modelling the fo:table-caption object.
 * @todo needs implementation
 */
public class TableCaption extends FObj {

    /** used for FO validation */
    private boolean blockItemFound = false;

    static boolean notImplementedWarningGiven = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public TableCaption(FONode parent) {
        super(parent);

        if (!notImplementedWarningGiven) {
            getLogger().warn("fo:table-caption is not yet implemented.");
            notImplementedWarningGiven = true;
        }
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL Content Model: marker* (%block;)
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) 
        throws SAXParseException {
        if (nsURI == FO_URI && localName.equals("marker")) {
            if (blockItemFound) {
               nodesOutOfOrderError(loc, "fo:marker", "(%block;)");
            }
        } else if (!isBlockItem(nsURI, localName)) {
            invalidChildError(loc, nsURI, localName);
        } else {
            blockItemFound = true;
        }
    }

    /**
     * Make sure content model satisfied, if so then tell the
     * FOEventHandler that we are at the end of the flow.
     * @see org.apache.fop.fo.FONode#end
     */
    protected void endOfNode() throws SAXParseException {
        if (childNodes == null) {
            missingChildElementError("marker* (%block;)");
        }
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:table-caption";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_TABLE_CAPTION;
    }
}

