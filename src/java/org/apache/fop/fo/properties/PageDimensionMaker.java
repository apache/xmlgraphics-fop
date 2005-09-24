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

package org.apache.fop.fo.properties;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.LengthProperty;

/**
 * Custom Maker for page-height / page-width
 * 
 */
public class PageDimensionMaker extends LengthProperty.Maker {
    
    /**
     * Constructor
     * 
     * @param propId    the property Id
     */
    public PageDimensionMaker(int propId) {
        super(propId);
    }
    
    /**
     * Check the value of the page-width / page-height property. 
     * Return the default or user-defined fallback in case the value
     * was specified as "auto"
     * 
     * @see org.apache.fop.fo.properties.PropertyMaker#get(int, PropertyList, boolean, boolean)
     */
    public Property get(int subpropId, PropertyList propertyList,
                        boolean tryInherit, boolean tryDefault) 
            throws PropertyException {
        
        Property p = super.get(0, propertyList, tryInherit, tryDefault);    
        
        //TODO: add check for both height and width being specified as indefinite
        //      and use the fallback value (= set to "auto")
        
        if (p.isAuto()) {
            FObj fo = propertyList.getFObj();
            
            String fallbackValue = (propId == Constants.PR_PAGE_HEIGHT)
                ? fo.getFOEventHandler().getUserAgent().getPageHeight()
                    : fo.getFOEventHandler().getUserAgent().getPageWidth();
            return make(propertyList, fallbackValue, fo);
        }
        
        return p;
    }
    
}
