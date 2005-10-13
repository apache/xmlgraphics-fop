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
 * Class for handling generic XML from a namespace not recognized by FOP
 */
public class UnknownXMLObj extends XMLObj {
    private String namespace;

    /**
     * Inner class for an UnknownXMLObj Maker
     */
    public static class Maker extends ElementMapping.Maker {
        private String space;

        /**
         * Construct the Maker
         * @param ns the namespace for this Maker
         */
        public Maker(String ns) {
            space = ns;
        }

        /**
         * Make an instance
         * @param parent FONode that is the parent of the object
         * @return the created UnknownXMLObj
         */
        public FONode make(FONode parent) {
            return new UnknownXMLObj(parent, space);
        }
    }

    /**
     * Constructs an unknown xml object (called by Maker).
     *
     * @param parent the parent formatting object
     * @param space the namespace for this object
     */
    protected UnknownXMLObj(FONode parent, String space) {
        super(parent);
        this.namespace = space;
    }

    /**
     * @see XMLObj#getNameSpace
     */
    public String getNameSpace() {
        return this.namespace;
    }

    /**
     * @see org.apache.fop.fo.FONode#addChildNode(FONode)
     */
    protected void addChildNode(FONode child) {
        if (doc == null) {
            createBasicDocument();
        }
        super.addChildNode(child);
    }

    /**
     *  @see XMLObj#addCharacters
     */
    protected void addCharacters(char data[], int start, int length,
                                 PropertyList pList, Locator locator) {
        if (doc == null) {
            createBasicDocument();
        }
        super.addCharacters(data, start, length, pList, locator);
    }
}

