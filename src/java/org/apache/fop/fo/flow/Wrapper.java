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

// Java
import java.util.List;
import java.util.ListIterator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.PropertyList;

/**
 * Implementation for fo:wrapper formatting object.
 * The wrapper object serves as
 * a property holder for its child node objects.
 *
 * Content: (#PCDATA|%inline;|%block;)*
 * Properties: id
 * @todo implement validateChildNode()
 */
public class Wrapper extends FObjMixed {
    // The value of properties relevant for fo:wrapper.
    private String id;
    // End of property values
    
    /**
     * @param parent FONode that is the parent of this object
     */
    public Wrapper(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#bind(PropertyList)
     */
    public void bind(PropertyList pList) throws FOPException {
        id = pList.get(PR_ID).getString();
    }

    /**
     * @see org.apache.fop.fo.FONode#startOfNode
     */
    protected void startOfNode() throws FOPException {
        checkId(id);
    }

    /**
     * Return the "id" property.
     */
    public String getId() {
        return id;
    }

    /**
     * @see org.apache.fop.fo.FONode#addLayoutManager(List)
     * @todo remove null check when vCN() & endOfNode() implemented
     */
    public void addLayoutManager(List list) {
        ListIterator baseIter = getChildNodes();
        if (baseIter == null) {
            return;
        }
        while (baseIter.hasNext()) {
            FONode child = (FONode) baseIter.next();
            child.addLayoutManager(list);
        }
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:wrapper";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_WRAPPER;
    }
}

