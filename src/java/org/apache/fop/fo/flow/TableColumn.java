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

// XML
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

// FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.FOTreeVisitor;

import org.apache.fop.fo.properties.CommonBackground;
import org.apache.fop.fo.properties.CommonBorderAndPadding;

/**
 * Class modelling the fo:table-column object. See Sec. 6.7.4 of the XSL-FO
 * Standard.
 */
public class TableColumn extends FObj {

    private ColorType backgroundColor;

    private Length columnWidth;
    private int columnOffset;
    private int numColumnsRepeated;
    private int iColumnNumber;

    private boolean initialized = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public TableColumn(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FObj#addProperties
     */
    protected void addProperties(Attributes attlist) throws FOPException {
        super.addProperties(attlist);
        initialize();    // init some basic property values
        getFOInputHandler().startColumn(this);
    }

    /**
     * @return Length object containing column width
     */
    public Length getColumnWidth() {
        return columnWidth;
    }

    /**
     * @return column number
     */
    public int getColumnNumber() {
        return iColumnNumber;
    }

    /**
     * @return value for number of columns repeated
     */
    public int getNumColumnsRepeated() {
        return numColumnsRepeated;
    }

    public void initialize() {

        // Common Border, Padding, and Background Properties
        // only background apply, border apply if border-collapse
        // is collapse.
        CommonBorderAndPadding bap = propMgr.getBorderAndPadding();
        CommonBackground bProps = propMgr.getBackgroundProps();

        // this.propertyList.get("column-width");
        // this.propertyList.get("number-columns-repeated");
        // this.propertyList.get("number-columns-spanned");
        // this.propertyList.get("visibility");

        iColumnNumber = propertyList.get(PR_COLUMN_NUMBER).getNumber().intValue();

        numColumnsRepeated =
            propertyList.get(PR_NUMBER_COLUMNS_REPEATED).getNumber().intValue();

        this.backgroundColor =
            this.propertyList.get(PR_BACKGROUND_COLOR).getColorType();

        columnWidth = this.propertyList.get(PR_COLUMN_WIDTH).getLength();

        // initialize id
        setupID();

        initialized = true;
    }

    /**
     * This is a hook for an FOTreeVisitor subclass to be able to access
     * this object.
     * @param fotv the FOTreeVisitor subclass that can access this object.
     * @see org.apache.fop.fo.FOTreeVisitor
     */
    public void acceptVisitor(FOTreeVisitor fotv) {
        fotv.serveTableColumn(this);
    }

    protected void endOfNode() throws SAXParseException {
        getFOInputHandler().endColumn(this);
    }
    
    public String getName() {
        return "fo:table-column";
    }
}

