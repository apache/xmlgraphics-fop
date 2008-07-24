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

package org.apache.fop.render.ps.extensions;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;

/**
 * Base postscript commment element class
 */
public abstract class AbstractPSCommentElement extends AbstractPSExtensionElement {

    /**
     * Default constructor
     *
     * @param parent parent of this node
     * @see org.apache.fop.fo.FONode#FONode(FONode)
     */
    public AbstractPSCommentElement(FONode parent) {
        super(parent);
    }

    /**
     * @throws FOPException if there's a problem during processing
     * @see org.apache.fop.fo.FONode#startOfNode()
     */
    protected void startOfNode() throws FOPException {
        if (parent.getNameId() != Constants.FO_DECLARATIONS
                && parent.getNameId() != Constants.FO_SIMPLE_PAGE_MASTER) {
            invalidChildError(getLocator(), parent.getName(), getNamespaceURI(), getName(),
                    "rule.childOfSPMorDeclarations");
        }
    }

}
