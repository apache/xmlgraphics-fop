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

// FOP
import org.apache.fop.apps.FOPException;

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
     * Process the attributes for this element.
     * @param attlist the attribute list for this element returned by the SAX
     * parser
     * @throws FOPException for invalid attributes
     */
    public void handleAttrs(Attributes attlist) throws FOPException {
        super.handleAttrs(attlist);
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
     * This is a hook for an FOTreeVisitor subclass to be able to access
     * this object.
     * @param fotv the FOTreeVisitor subclass that can access this object.
     * @see org.apache.fop.fo.FOTreeVisitor
     */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveXMLElement(this);
    }

}
