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

// XML
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.layoutmgr.AddLMVisitor;

/**
 * class representing svg:svg pseudo flow object.
 */
public class XMLElement extends XMLObj {

    private String namespace = "";

    /**
     * constructs an XML object (called by Maker).
     *
     * @param parent the parent formatting object
     */
    public XMLElement(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FONode#processNode
     */
    public void processNode(String elementName, Locator locator, 
                            Attributes attlist) throws FOPException {
        super.processNode(elementName, locator, attlist);
        init();
    }

    private void init() {
        createBasicDocument();
    }

    /**
     * Public accessor for the namespace.
     * @return the namespace for this element
     */
    public String getNameSpace() {
        return namespace;
    }

    /**
     * This is a hook for the AddLMVisitor class to be able to access
     * this object.
     * @param aLMV the AddLMVisitor object that can access this object.
     */
    public void acceptVisitor(AddLMVisitor aLMV) {
        aLMV.serveXMLElement(this);
    }

}
