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
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.KeepValue;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.layoutmgr.table.Row;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.properties.Property;


/**
 * Class modelling the fo:table-row object.
 * @todo implement validateChildNode()
 */
public class TableRow extends FObj {

    private boolean setup = false;

    private int breakAfter;
    private ColorType backgroundColor;

    private KeepValue keepWithNext;
    private KeepValue keepWithPrevious;
    private KeepValue keepTogether;

    private int minHeight = 0;    // force row height

    /**
     * @param parent FONode that is the parent of this object
     */
    public TableRow(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws SAXParseException {
        super.addProperties(attlist);
        getFOEventHandler().startRow(this);
    }


    protected void endOfNode() throws SAXParseException {
        getFOEventHandler().endRow(this);
    }

    /**
     * @return keepWithPrevious
     */
    public KeepValue getKeepWithPrevious() {
        return keepWithPrevious;
    }

    private void doSetup() {
        this.breakAfter = getPropEnum(PR_BREAK_AFTER);
        this.backgroundColor =
            this.propertyList.get(PR_BACKGROUND_COLOR).getColorType();

        this.keepTogether = getKeepValue(PR_KEEP_TOGETHER | CP_WITHIN_COLUMN);
        this.keepWithNext = getKeepValue(PR_KEEP_WITH_NEXT | CP_WITHIN_COLUMN);
        this.keepWithPrevious =
            getKeepValue(PR_KEEP_WITH_PREVIOUS | CP_WITHIN_COLUMN);

        this.minHeight = getPropLength(PR_HEIGHT);
        setup = true;
    }

    private KeepValue getKeepValue(int propId) {
        Property p = this.propertyList.get(propId);
        Number n = p.getNumber();
        if (n != null) {
            return new KeepValue(KeepValue.KEEP_WITH_VALUE, n.intValue());
        }
        switch (p.getEnum()) {
        case Constants.ALWAYS:
            return new KeepValue(KeepValue.KEEP_WITH_ALWAYS, 0);
        case Constants.AUTO:
        default:
            return new KeepValue(KeepValue.KEEP_WITH_AUTO, 0);
        }
    }

    /**
     * @see org.apache.fop.fo.FObj#addLayoutManager(List)
     */
    public void addLayoutManager(List list) { 	 
        Row rlm = new Row(this);
        list.add(rlm); 	 
    }
    
    /**
     * @see org.apache.fop.fo.FObj#getName()
     */
    public String getName() {
        return "fo:table-row";
    }

    /**
     * @see org.apache.fop.fo.FObj#getNameId()
     */
    public int getNameId() {
        return FO_TABLE_ROW;
    }
}
