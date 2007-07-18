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
     * @see PropertyMaker#get(int, PropertyList, boolean, boolean)
     */
    public Property get(int subpropId, PropertyList propertyList,
                        boolean tryInherit, boolean tryDefault) 
            throws PropertyException {
        
        Property p = super.get(0, propertyList, tryInherit, tryDefault);    
        FObj fo = propertyList.getFObj();
        String fallbackValue = (propId == Constants.PR_PAGE_HEIGHT)
            ? fo.getFOEventHandler().getUserAgent().getPageHeight()
                    : fo.getFOEventHandler().getUserAgent().getPageWidth();
        
        if (p.getEnum() == Constants.EN_INDEFINITE) {
            int otherId = (propId == Constants.PR_PAGE_HEIGHT) 
                ? Constants.PR_PAGE_WIDTH : Constants.PR_PAGE_HEIGHT;
            int writingMode = propertyList.get(Constants.PR_WRITING_MODE).getEnum();
            int refOrientation = propertyList.get(Constants.PR_REFERENCE_ORIENTATION)
                        .getNumeric().getValue();
            if (propertyList.getExplicit(otherId) != null
                    && propertyList.getExplicit(otherId).getEnum() == Constants.EN_INDEFINITE) {
                //both set to "indefinite":
                //determine which one of the two defines the dimension
                //in block-progression-direction, and set the other to 
                //"auto"
                if ((writingMode != Constants.EN_TB_RL
                        && (refOrientation == 0 
                                || refOrientation == 180
                                || refOrientation == -180))
                     || (writingMode == Constants.EN_TB_RL
                             && (refOrientation == 90
                                     || refOrientation == 270
                                     || refOrientation == -270))) {
                    //set page-width to "auto" = use the fallback from FOUserAgent
                    if (propId == Constants.PR_PAGE_WIDTH) {
                        Property.log.warn("Both page-width and page-height set to "
                                + "\"indefinite\". Forcing page-width to \"auto\"");
                        return make(propertyList, fallbackValue, fo);
                    }
                } else {
                    //set page-height to "auto" = use fallback from FOUserAgent
                    Property.log.warn("Both page-width and page-height set to "
                            + "\"indefinite\". Forcing page-height to \"auto\"");
                    if (propId == Constants.PR_PAGE_HEIGHT) {
                        return make(propertyList, fallbackValue, fo);
                    }
                }
            }
        } else if (p.isAuto()) {
            return make(propertyList, fallbackValue, fo);
        }
        
        return p;
    }    
}
