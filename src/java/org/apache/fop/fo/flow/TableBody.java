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

// XML
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.layoutmgr.table.Body;

/**
 * Class modelling the fo:table-body object.
 * @todo implement validateChildNode()
 */
public class TableBody extends FObj {

    private int spaceBefore;
    private int spaceAfter;
    private ColorType backgroundColor;

    /**
     * @param parent FONode that is the parent of the object
     */
    public TableBody(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);
        this.spaceBefore = getPropLength(PR_SPACE_BEFORE | CP_OPTIMUM);
        this.spaceAfter = getPropLength(PR_SPACE_AFTER | CP_OPTIMUM);
        this.backgroundColor =
          this.propertyList.get(PR_BACKGROUND_COLOR).getColorType();
        getFOEventHandler().startBody(this);
    }

    protected void endOfNode() throws SAXParseException {
        getFOEventHandler().endBody(this);
    }

    /**
     * @see org.apache.fop.fo.FObj#addLayoutManager(List)
     */
    public void addLayoutManager(List list) { 	 
        Body blm = new Body(this);
        list.add(blm);
    }

    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:table-body";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_TABLE_BODY;
    }
}

