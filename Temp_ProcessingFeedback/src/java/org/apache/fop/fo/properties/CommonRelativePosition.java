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
 * Store all common relative position properties.
 * See Sec 7.12 of the XSL-FO Standard.
 * Public "structure" allows direct member access.
 */
public class CommonRelativePosition {
    /**
     * The "relative-position" property.
     */
    public int relativePosition;
    
    /**
     * The "top" property.
     */
    public Length top;

    /**
     * The "right" property.
     */
    public Length right;
    
    /**
     * The "bottom" property.
     */
    public Length bottom;
    
    /**
     * The "left" property.
     */
    public Length left;

    /**
     * Create a CommonRelativePosition object.
     * @param pList The PropertyList with propery values.
     */
    public CommonRelativePosition(PropertyList pList) throws PropertyException {
        relativePosition = pList.get(Constants.PR_RELATIVE_POSITION).getEnum();
        top = pList.get(Constants.PR_TOP).getLength();
        bottom = pList.get(Constants.PR_BOTTOM).getLength();
        left = pList.get(Constants.PR_LEFT).getLength();
        right = pList.get(Constants.PR_RIGHT).getLength();      
    }

}
