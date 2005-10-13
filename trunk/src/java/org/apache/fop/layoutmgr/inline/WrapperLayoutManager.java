/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.fop.layoutmgr.inline;

import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.fo.flow.Wrapper;
import org.apache.fop.layoutmgr.LayoutContext;

/**
 * This is the layout manager for the fo:wrapper formatting object.
 */
public class WrapperLayoutManager extends LeafNodeLayoutManager {
    
    private Wrapper fobj;

    /**
     * Creates a new LM for fo:wrapper.
     * @param node the fo:wrapper
     */
    public WrapperLayoutManager(Wrapper node) {
        super(node);
        fobj = node;
    }

    /** @see org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager */
    public InlineArea get(LayoutContext context) {
        //Create a zero-width, zero-height dummy area so this node can 
        //participate in the ID handling. Otherwise, addId() wouldn't 
        //be called.
        InlineArea area = new InlineArea();
        return area;
    }
    
    /** @see org.apache.fop.layoutmgr.inline.LeafNodeLayoutManager#addId() */
    protected void addId() {
        getPSLM().addIDToPage(fobj.getId());
    }
    
}
