/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.fop.fo.properties;

import org.apache.fop.datatypes.Length;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;

/**
 * Store all common margin properties for blocks.
 * See Sec. 7.10 of the XSL-FO Standard.
 * Public "structure" allows direct member access.
 */
public class CommonMarginBlock {
    /**
     * The "margin-top" property.
     */
    public Length marginTop;

    /**
     * The "margin-bottom" property.
     */
    public Length marginBottom;

    /**
     * The "margin-left" property.
     */
    public Length marginLeft;

    /**
     * The "margin-right" property.
     */
    public Length marginRight;

    /**
     * The "space-before" property.
     */
    public SpaceProperty spaceBefore;

    /**
     * The "space-after" property.
     */
    public SpaceProperty spaceAfter;

    /**
     * The "start-indent" property.
     */
    public Length startIndent;

    /**
     * The "end-indent" property.
     */
    public Length endIndent;

    /**
     * Create a CommonMarginBlock object.
     * @param pList The PropertyList with propery values.
     */
    public CommonMarginBlock(PropertyList pList) throws PropertyException {
        marginTop = pList.get(Constants.PR_MARGIN_TOP).getLength();
        marginBottom = pList.get(Constants.PR_MARGIN_BOTTOM).getLength();
        marginLeft = pList.get(Constants.PR_MARGIN_LEFT).getLength();
        marginRight = pList.get(Constants.PR_MARGIN_RIGHT).getLength();

        spaceBefore = pList.get(Constants.PR_SPACE_BEFORE).getSpace();
        spaceAfter = pList.get(Constants.PR_SPACE_AFTER).getSpace();

        startIndent = pList.get(Constants.PR_START_INDENT).getLength();
        endIndent = pList.get(Constants.PR_END_INDENT).getLength();
    }
    
    /** {@inheritDoc} */
    public String toString() {
        return "CommonMarginBlock:\n" 
            + "Margins (top, bottom, left, right): (" 
            + marginTop + ", " + marginBottom + ", " 
            + marginLeft + ", " + marginRight + ")\n"
            + "Space (before, after): (" 
            + spaceBefore + ", " + spaceAfter + ")\n" 
            + "Indents (start, end): ("
            + startIndent + ", " + endIndent + ")\n";
    }
    
}
