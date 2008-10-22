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

import org.apache.fop.apps.FOPException;
//import org.apache.fop.fo.Constants;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
//import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.extensions.ExtensionAttachment;
import org.apache.fop.fo.PropertyList;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

/**
 * Extension element for afp:resource-group
 */
public class AFPResourceInfoElement extends AbstractAFPExtensionObject {
    
    /**
     * Main constructor
     * @param parent parent FO node
     */
    public AFPResourceInfoElement(FONode parent) {
        super(parent, AFPResourceInfo.ELEMENT);
    }
    
    /**
     * @throws FOPException if there's a problem during processing
     * @see org.apache.fop.fo.FONode#startOfNode() 
     */
    protected void startOfNode() throws FOPException {
        if (parent.getNameId() != Constants.FO_INSTREAM_FOREIGN_OBJECT
                && parent.getNameId() != Constants.FO_EXTERNAL_GRAPHIC) {
            invalidChildError(getLocator(), parent.getName(), getNamespaceURI(), getName(),
                    "rule.childOfInstreamForeignObjectorExternalGraphic");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void processNode(String elementName, Locator locator,
                            Attributes attlist, PropertyList propertyList)
                                throws FOPException {
        getExtensionAttachment();
        String attr = attlist.getValue("name");
        if (attr != null && attr.length() > 0) {
            extensionAttachment.setName(attr);
        } else {
            throw new FOPException(elementName + " must have a name attribute.");
        }
        String lvl = attlist.getValue("level");
        AFPResourceInfo resourceInfo = (AFPResourceInfo)getExtensionAttachment();
        if (lvl != null && lvl.length() > 0) {
            resourceInfo.setLevel(lvl);
            if (resourceInfo.isExternalLevel()) {
                String dest = attlist.getValue("dest");
                if (dest != null && dest.length() > 0) {
                    resourceInfo.setExternalDestination(dest);
                } else {
                    throw new FOPException("must have a dest attribute.");
                }                
            }
        } else {
            throw new FOPException("must have a level attribute.");
        }
    }

    /**
     * {@inheritDoc}
     */
    protected ExtensionAttachment instantiateExtensionAttachment() {
        return new AFPResourceInfo();
    }
}