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

package org.apache.fop.fo.pagination;

// XML
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FOElementMapping;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTreeVisitor;

/**
 * Class modelling the fo:static-content object. See Sec. 6.4.19 of the XSL-FO
 * Standard.
 */
public class StaticContent extends Flow {

    /**
     * @param parent FONode that is the parent of this object
     */
    public StaticContent(FONode parent) {
        super(parent);
    }

    private void setup() {
    }

    /**
     * @see org.apache.fop.fo.FONode#validateChildNode(Locator, String, String)
     * XSL/FOP Content Model: (%block;)+
     */
    protected void validateChildNode(Locator loc, String nsURI, String localName) {
        if (!isBlockItem(nsURI, localName)) {
            invalidChildError(loc, nsURI, localName);
        }
    }

    /**
     * Make sure content model satisfied, if so then tell the
     * StructureRenderer that we are at the end of the flow.
     * @see org.apache.fop.fo.FONode#end
     */
    protected void end() {
        if (childNodes == null) {
            missingChildElementError("(%block;)+");
        }
        getFOInputHandler().endFlow(this);
    }

    /**
     * flowname checking is more stringient for static content currently
     * @param name the flow-name to set
     * @throws FOPException for a missing flow name
     */
    protected void setFlowName(String name) throws FOPException {
        if (name == null || name.equals("")) {
            throw new FOPException("A 'flow-name' is required for "
                                   + getName() + ".");
        } else {
            super.setFlowName(name);
        }

    }

    /**
     * This is a hook for an FOTreeVisitor subclass to be able to access
     * this object.
     * @param fotv the FOTreeVisitor subclass that can access this object.
     * @see org.apache.fop.fo.FOTreeVisitor
     */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveStaticContent(this);
    }

    public String getName() {
        return "fo:static-content";
    }
}
