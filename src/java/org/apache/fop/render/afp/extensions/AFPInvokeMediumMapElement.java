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
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.extensions.ExtensionAttachment;

/**
 * This class represents an AFP-specific extension element to embed Invoke Medium Map (IMM)
 * fields at the beginning of a page group or just prior to a Page. The element is optional
 * and expected as a direct child of an fo:page-sequence or fo:simple-page-master
 */
public class AFPInvokeMediumMapElement extends AbstractAFPExtensionObject {

    /**
     * Constructs the AFP extension object (called by Maker).
     * @param parent the parent formatting object
     */
    public AFPInvokeMediumMapElement(FONode parent) {
        super(parent, AFPElementMapping.INVOKE_MEDIUM_MAP);
    }

    /** {@inheritDoc} */
    public void startOfNode() throws FOPException {
        super.startOfNode();
        if (parent.getNameId() != Constants.FO_PAGE_SEQUENCE
                && parent.getNameId() != Constants.FO_SIMPLE_PAGE_MASTER) {

            invalidChildError(getLocator(), parent.getName(), getNamespaceURI(), getName(),
                "rule.childOfPageSequence");
        }
    }

    /** {@inheritDoc} */
    protected ExtensionAttachment instantiateExtensionAttachment() {
        return new AFPInvokeMediumMap();
    }
}
