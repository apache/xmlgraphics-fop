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
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.extensions.ExtensionAttachment;

/**
 * This class extends the org.apache.fop.extensions.ExtensionObj class. The
 * object faciliates extraction of elements from formatted objects based on
 * the static list as defined in the AFPElementMapping implementation.
 * <p/>
 */
public class AFPElement extends AbstractAFPExtensionObject {

    /**
     * Constructs an AFP object (called by Maker).
     *
     * @param parent the parent formatting object
     * @param name the name of the afp element
     */
    public AFPElement(FONode parent, String name) {
        super(parent, name);
    }

    /** {@inheritDoc} */
    protected void startOfNode() throws FOPException {
        super.startOfNode();
        //if (!AFPElementMapping.NAMESPACE.equals(parent.getNamespaceURI())
        //    || !AFPElementMapping.PAGE.equals(parent.getLocalName())) {
        //    throw new ValidationException(getName() + " must be a child of afp:page.");
        //}
        if (parent.getNameId() != Constants.FO_SIMPLE_PAGE_MASTER) {
            throw new ValidationException(getName() + " must be a child of fo:simple-page-master.");
        }
    }

	protected ExtensionAttachment instantiateExtensionAttachment() {
		return new AFPPageSetup(getName());
	}

}
