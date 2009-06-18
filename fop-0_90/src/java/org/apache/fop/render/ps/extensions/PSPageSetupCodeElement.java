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

package org.apache.fop.render.ps.extensions;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.ValidationException;

/**
 * Extension element for fox:ps-page-setup-code. 
 */
public class PSPageSetupCodeElement extends AbstractPSExtensionObject {

    /**
     * Main constructor
     * @param parent parent FO node
     */
    public PSPageSetupCodeElement(FONode parent) {
        super(parent);
    }

    /** @see org.apache.fop.fo.FONode#startOfNode() */
    protected void startOfNode() throws FOPException {
        super.startOfNode();
        if (parent.getNameId() != Constants.FO_SIMPLE_PAGE_MASTER) {
            throw new ValidationException(getName() + " must be a child of fo:simple-page-master.");
        }
    }
    
    /** @see org.apache.fop.fo.FONode#getLocalName() */
    public String getLocalName() {
        return "ps-page-setup-code";
    }

}
