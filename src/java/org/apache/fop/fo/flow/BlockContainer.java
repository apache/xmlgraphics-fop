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

// FOP
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.layoutmgr.BlockContainerLayoutManager;

import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

/**
 * Class modelling the fo:block-container object.
 * @todo implement validateChildNode()
 */
public class BlockContainer extends FObj {

    private ColorType backgroundColor;
    private int position;

    private int top;
    private int bottom;
    private int left;
    private int right;
    private int width;
    private int height;

    private int span;

    /**
     * @param parent FONode that is the parent of this object
     */
    public BlockContainer(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);
        this.span = getPropEnum(PR_SPAN);
        this.backgroundColor =
            this.propertyList.get(PR_BACKGROUND_COLOR).getColorType();

        this.width = getPropLength(PR_WIDTH);
        this.height = getPropLength(PR_HEIGHT);
        getFOEventHandler().startBlockContainer(this);
    }
    
    /**
     * @see org.apache.fop.fo.FONode#endOfNode
     */
    protected void endOfNode() throws SAXParseException {
        getFOEventHandler().endBlockContainer(this);
    }

    /**
     * @return true (BlockContainer can generate Reference Areas)
     */
    public boolean generatesReferenceAreas() {
        return true;
    }

    /**
     * @return the span for this object
     */
    public int getSpan() {
        return this.span;
    }

    /**
     * @see org.apache.fop.fo.FObj#addLayoutManager(List)
     */
    public void addLayoutManager(List list) {    
        BlockContainerLayoutManager blm = new BlockContainerLayoutManager(this);
        blm.setOverflow(getPropEnum(PR_OVERFLOW));
        list.add(blm);
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:block-container";
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_BLOCK_CONTAINER;
    }
}

