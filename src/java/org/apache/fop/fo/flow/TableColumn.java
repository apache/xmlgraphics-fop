/*
 * $Id: TableColumn.java,v 1.29 2003/03/05 20:38:21 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.datatypes.ColorType;
import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.Property;

import org.apache.fop.layout.BackgroundProps;
import org.apache.fop.layout.BorderAndPadding;
import org.apache.fop.layoutmgr.LayoutManager;
import org.apache.fop.layoutmgr.table.Column;

/**
 * Class modelling the fo:table-column object. See Sec. 6.7.4 of the XSL-FO
 * Standard.
 */
public class TableColumn extends FObj {

    private ColorType backgroundColor;

    private Length columnWidthPropVal;
    private int columnWidth;
    private int columnOffset;
    private int numColumnsRepeated;
    private int iColumnNumber;

    private boolean setup = false;

    /**
     * @param parent FONode that is the parent of this object
     */
    public TableColumn(FONode parent) {
        super(parent);
    }

    public LayoutManager getLayoutManager() {
        doSetup();
        Column clm = new Column();
        clm.setUserAgent(getUserAgent());
        clm.setFObj(this);
        return clm;
    }

    /**
     * @return Length object containing column width
     */
    public Length getColumnWidthAsLength() {
        return columnWidthPropVal;
    }

    /**
     * @return the column width (in millipoints ??)
     */
    public int getColumnWidth() {
        return columnWidth;
    }

    /**
     * Set the column width value, overriding the value from the column-width
     * Property.
     * @param columnWidth the column width value in base units (millipoints ??)
     */
    public void setColumnWidth(int columnWidth) {
        this.columnWidth = columnWidth;
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

    private void doSetup() {

        // Common Border, Padding, and Background Properties
        // only background apply, border apply if border-collapse
        // is collapse.
        BorderAndPadding bap = propMgr.getBorderAndPadding();
        BackgroundProps bProps = propMgr.getBackgroundProps();

        // this.properties.get("column-width");
        // this.properties.get("number-columns-repeated");
        // this.properties.get("number-columns-spanned");
        // this.properties.get("visibility");

        iColumnNumber = properties.get("column-number").getNumber().intValue();

        numColumnsRepeated =
            properties.get("number-columns-repeated").getNumber().intValue();

        this.backgroundColor =
            this.properties.get("background-color").getColorType();

        Property prop = this.properties.get("column-width");
        if (prop != null) {
            columnWidthPropVal = properties.get("column-width").getLength();

            // This won't include resolved table-units or % values yet.
            columnWidth = columnWidthPropVal.getValue();
        } else {
            columnWidth = 300000;
        }

        // initialize id
        setupID();

        setup = true;
    }

}

