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

package org.apache.fop.render.afp.extensions;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.afp.AFPPaintingState;
import org.apache.fop.afp.AFPUnitConverter;
import org.apache.xmlgraphics.util.UnitConv;

/**
 * This class extends the org.apache.fop.render.afp.extensions.AbstractAFPExtensionObject class.
 * This object will be used to map the page overlay object in  AFPElementMapping.
 * <p/>
 */
public class AFPPageOverlayElement extends AbstractAFPExtensionObject {

    private static final String ATT_X = "x";
    private static final String ATT_Y = "y";

    /**
     * Constructs an AFP object (called by Maker).
     *
     * @param parent the parent formatting object
     * @param name the name of the afp element
     */
    public AFPPageOverlayElement(FONode parent, String name) {
        super(parent, name);
    }

    private AFPPageOverlay getPageSetupAttachment() {
        return (AFPPageOverlay)getExtensionAttachment();
    }

    /** {@inheritDoc} */
    protected void startOfNode() throws FOPException {
        super.startOfNode();
        if (AFPElementMapping.INCLUDE_PAGE_OVERLAY.equals(getLocalName())) {
            if (parent.getNameId() != Constants.FO_SIMPLE_PAGE_MASTER
                    && parent.getNameId() != Constants.FO_PAGE_SEQUENCE) {
                invalidChildError(getLocator(), parent.getName(), getNamespaceURI(), getName(),
                    "rule.childOfPageSequenceOrSPM");
            }
        } else {
            if (parent.getNameId() != Constants.FO_SIMPLE_PAGE_MASTER) {
                invalidChildError(getLocator(), parent.getName(), getNamespaceURI(), getName(),
                    "rule.childOfSPM");
            }
        }
    }


    /** {@inheritDoc} */
    public void processNode(String elementName, Locator locator,
                            Attributes attlist, PropertyList propertyList)
                                throws FOPException {
        super.processNode(elementName, locator, attlist, propertyList);
        AFPPageOverlay pageOverlay = getPageSetupAttachment();
        if (AFPElementMapping.INCLUDE_PAGE_OVERLAY.equals(elementName)) {
            // convert user specific units to mpts and set the coordinates for the page overlay
            AFPPaintingState paintingState = new AFPPaintingState();
            AFPUnitConverter unitConverter = new AFPUnitConverter(paintingState);
            int x = (int)unitConverter.mpt2units(UnitConv.convert(attlist.getValue(ATT_X)));
            int y = (int)unitConverter.mpt2units(UnitConv.convert(attlist.getValue(ATT_Y)));
            pageOverlay.setX(x);
            pageOverlay.setY(y);
        }
    }

    /** {@inheritDoc} */
    protected ExtensionAttachment instantiateExtensionAttachment() {
        return new AFPPageOverlay();
    }
}
