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

import org.apache.fop.layoutmgr.AddLMVisitor;

/**
 * Class for handling an unknown element, for example one from an unsupported
 * namespace.
 * This prevents any further problems arising from the unknown
 * data.
 */
public class Unknown extends FONode {

    /**
     * Inner class for handling the creation of Unknown objects
     */
    public static class Maker extends ElementMapping.Maker {

        /**
         * @param parent the FONode that is the parent of the object to be
         * created
         * @return the created Unknown object
         */
        public FONode make(FONode parent) {
            return new Unknown(parent);
        }
    }

    /**
     * @param parent FONode that is the parent of this object
     */
    public Unknown(FONode parent) {
        super(parent);
    }

    private void setup() {
        getLogger().debug("Layout Unknown element");
    }

    /**
     * This is a hook for the AddLMVisitor class to be able to access
     * this object.
     * @param aLMV the AddLMVisitor object that can access this object.
     */
    public void acceptVisitor(AddLMVisitor aLMV) {
        aLMV.serveUnknown(this);
    }

}
