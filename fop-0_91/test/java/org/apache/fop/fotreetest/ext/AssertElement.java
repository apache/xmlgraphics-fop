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
 
package org.apache.fop.fotreetest.ext;


import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOPropertyMapping;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.properties.LengthPairProperty;
import org.apache.fop.fo.properties.LengthRangeProperty;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fotreetest.ResultCollector;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

/**
 * Defines the assert element for the FOP Test extension.
 */
public class AssertElement extends TestObj {

    /**
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public AssertElement(FONode parent) {
        super(parent);
    }

    /**
     * @see org.apache.fop.fo.FONode#processNode
     */
    public void processNode(String elementName, 
                            Locator locator, 
                            Attributes attlist, 
                            PropertyList propertyList) throws FOPException {
        //super.processNode(elementName, locator, attlist, propertyList);

        ResultCollector collector = ResultCollector.getInstance();
        String propName = attlist.getValue("property");
        String component = null;
        int dotIndex = propName.indexOf('.');
        if (dotIndex >= 0) {
            component = propName.substring(dotIndex + 1);
            propName = propName.substring(0, dotIndex);
        }
        int propID = FOPropertyMapping.getPropertyId(propName);
        if (propID < 0) {
            collector.notifyException(new IllegalArgumentException(
                    "Property not found: " + propName));
        } else {
            Property prop;
            prop = propertyList.getParentPropertyList().get(propID);
            if (component != null) {
                //Access subcomponent
                Property mainProp = prop;
                prop = null;
                LengthPairProperty lpp = mainProp.getLengthPair();
                if (lpp != null) {
                    prop = lpp.getComponent(FOPropertyMapping.getSubPropertyId(component));
                }
                LengthRangeProperty lrp = mainProp.getLengthRange();
                if (lrp != null) {
                    prop = lrp.getComponent(FOPropertyMapping.getSubPropertyId(component));
                }
            }
            String s = String.valueOf(prop);
            String expected = attlist.getValue("expected");
            if (!expected.equals(s)) {
                collector.notifyException(new IllegalStateException("Property '" + propName 
                        + "' expected to evaluate to '" + expected + "' but got: " + s
                        + "\nLine #" + locator.getLineNumber() 
                        + " Column #" + locator.getColumnNumber()));
            }
        }
        
    }

    /** @see org.apache.fop.fo.FONode#getLocalName() */
    public String getLocalName() {
        return "assert";
    }
    
}

