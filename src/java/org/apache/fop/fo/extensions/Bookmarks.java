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

package org.apache.fop.fo.extensions;

// Java
import java.util.ArrayList;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.pagination.Root;

/**
 * Bookmarks data is the top level element of the pdf bookmark extension.
 * This handles the adding of outlines. When the element is ended it
 * creates the bookmark data and adds to the area tree.
 */
public class Bookmarks extends ExtensionObj {
    private ArrayList outlines = new ArrayList();

    /**
     * Create a new Bookmarks object.
     *
     * @param parent the parent fo node
     */
    public Bookmarks(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FONode#addChildNode(FONode)
     */
    protected void addChildNode(FONode obj) {
        if (obj instanceof Outline) {
            outlines.add(obj);
        }
    }

    /**
     * When this element is finished then it can create
     * the bookmark data from the child elements and add
     * the extension to the area tree.
     */
    protected void endOfNode() throws FOPException {
        ((Root) parent).setBookmarks(this);
    }

    public ArrayList getOutlines() {
        return outlines;
    }
}
